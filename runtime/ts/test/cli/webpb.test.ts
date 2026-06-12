import { symlinkSync, unlinkSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { fileURLToPath, pathToFileURL } from "node:url";
import * as generate from "../../src/cli/generate";
import {
  HELP,
  bootstrapMain,
  isMainModule,
  runCli,
  runMainEntrypoint,
} from "../../src/cli/bin/webpb";

describe("runCli", () => {
  let stdoutSpy: ReturnType<typeof vi.spyOn>;
  let stderrSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    stdoutSpy = vi.spyOn(process.stdout, "write").mockImplementation(() => true);
    stderrSpy = vi.spyOn(console, "error").mockImplementation(() => undefined);
  });

  afterEach(() => {
    stdoutSpy.mockRestore();
    stderrSpy.mockRestore();
    vi.restoreAllMocks();
  });

  it("should print help and return 0 when no command is provided", async () => {
    await expect(runCli([])).resolves.toBe(0);
    expect(stdoutSpy).toHaveBeenCalledWith(HELP);
  });

  it("should print help when -h is passed", async () => {
    await expect(runCli(["-h"])).resolves.toBe(0);
    expect(stdoutSpy).toHaveBeenCalledWith(HELP);
  });

  it("should delegate to runGenerate when command is generate", async () => {
    vi.spyOn(generate, "runGenerate").mockResolvedValue(0);
    await expect(runCli(["generate", "--help"])).resolves.toBe(0);
    expect(generate.runGenerate).toHaveBeenCalledWith(["--help"]);
  });

  it("should return 1 when command is unknown", async () => {
    await expect(runCli(["unknown"])).resolves.toBe(1);
    expect(stderrSpy).toHaveBeenCalledWith("Unknown command: unknown");
    expect(stdoutSpy).toHaveBeenCalledWith(HELP);
  });
});

describe("isMainModule", () => {
  it("should return true when argv matches module path", () => {
    const modulePath = fileURLToPath(
      new URL("../../src/cli/bin/webpb.ts", import.meta.url),
    );
    expect(isMainModule(pathToFileURL(modulePath), ["node", modulePath])).toBe(
      true,
    );
  });

  it("should return false when argv does not match module path", () => {
    const modulePath = fileURLToPath(
      new URL("../../src/cli/bin/webpb.ts", import.meta.url),
    );
    expect(isMainModule(pathToFileURL(modulePath), ["node", "/other/path"])).toBe(
      false,
    );
  });

  it("should return true when argv is a symlink to the module path", () => {
    if (process.platform === "win32") {
      // Windows symlink semantics and realpath prefixes differ from Unix.
      return;
    }
    const modulePath = fileURLToPath(
      new URL("../../src/cli/bin/webpb.ts", import.meta.url),
    );
    const linkPath = join(tmpdir(), `webpb-bin-${process.pid}`);
    try {
      symlinkSync(modulePath, linkPath);
    } catch {
      // Symlink creation may require elevated privileges on Windows.
      return;
    }
    try {
      expect(isMainModule(pathToFileURL(modulePath), ["node", linkPath])).toBe(
        true,
      );
    } finally {
      unlinkSync(linkPath);
    }
  });

  it("should compare paths case-insensitively on windows", () => {
    const modulePath = fileURLToPath(
      new URL("../../src/cli/bin/webpb.ts", import.meta.url),
    );
    const platformSpy = vi.spyOn(process, "platform", "get").mockReturnValue("win32");
    expect(
      isMainModule(pathToFileURL(modulePath), [
        "node",
        modulePath.toUpperCase(),
      ]),
    ).toBe(true);
    platformSpy.mockRestore();
  });

  it("should return false when argv entry is missing", () => {
    const modulePath = fileURLToPath(
      new URL("../../src/cli/bin/webpb.ts", import.meta.url),
    );
    expect(isMainModule(pathToFileURL(modulePath), ["node"])).toBe(false);
  });
});

describe("runMainEntrypoint", () => {
  it("should return undefined when module is not the entrypoint", async () => {
    const modulePath = fileURLToPath(
      new URL("../../src/cli/bin/webpb.ts", import.meta.url),
    );
    await expect(
      runMainEntrypoint(["node", "/other/path"], pathToFileURL(modulePath)),
    ).resolves.toBeUndefined();
  });

  it("should run cli when module is the entrypoint", async () => {
    const modulePath = fileURLToPath(
      new URL("../../src/cli/bin/webpb.ts", import.meta.url),
    );
    vi.spyOn(generate, "runGenerate").mockResolvedValue(0);
    await expect(
      runMainEntrypoint(["node", modulePath, "generate", "--help"], pathToFileURL(modulePath)),
    ).resolves.toBe(0);
  });
});

describe("bootstrapMain", () => {
  it("should do nothing when module is not the entrypoint", async () => {
    const modulePath = fileURLToPath(
      new URL("../../src/cli/bin/webpb.ts", import.meta.url),
    );
    const exitSpy = vi.spyOn(process, "exit").mockImplementation(() => undefined as never);
    await bootstrapMain(["node", "/other/path"], pathToFileURL(modulePath));
    expect(exitSpy).not.toHaveBeenCalled();
    exitSpy.mockRestore();
  });

  it("should exit with cli status when bootstrapped as main", async () => {
    const modulePath = fileURLToPath(
      new URL("../../src/cli/bin/webpb.ts", import.meta.url),
    );
    const exitSpy = vi.spyOn(process, "exit").mockImplementation(() => undefined as never);
    vi.spyOn(generate, "runGenerate").mockResolvedValue(0);
    await bootstrapMain(
      ["node", modulePath, "generate", "--help"],
      pathToFileURL(modulePath),
    );
    expect(exitSpy).toHaveBeenCalledWith(0);
    exitSpy.mockRestore();
  });
});
