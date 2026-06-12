import { spawnSync } from "node:child_process";
import { existsSync, mkdirSync, mkdtempSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";
import { runGenerate } from "../../src/cli/generate";
import * as resolveProtoc from "../../src/cli/resolve-protoc";
import * as resolveProtocPlugin from "../../src/cli/resolve-protoc-plugin";

vi.mock("node:child_process", () => ({
  spawnSync: vi.fn(),
}));

vi.mock("node:fs", async (importOriginal) => {
  const actual = await importOriginal<typeof import("node:fs")>();
  return {
    ...actual,
    existsSync: vi.fn(),
    mkdirSync: vi.fn(),
  };
});

describe("runGenerate", () => {
  let tempDir = "";
  let protoFile = "";
  let stdoutSpy: ReturnType<typeof vi.spyOn>;
  let stderrSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    tempDir = mkdtempSync(join(tmpdir(), "webpb-generate-"));
    protoFile = join(tempDir, "Sample.proto");
    writeFileSync(protoFile, "syntax = \"proto3\";\n");
    stdoutSpy = vi.spyOn(process.stdout, "write").mockImplementation(() => true);
    stderrSpy = vi.spyOn(console, "error").mockImplementation(() => undefined);
    vi.spyOn(resolveProtoc, "resolveProtoc").mockResolvedValue("/bin/protoc");
    vi.spyOn(resolveProtocPlugin, "resolveProtocPlugin").mockResolvedValue(
      "/bin/plugin",
    );
    vi.mocked(existsSync).mockImplementation(
      (path) => String(path) === protoFile,
    );
    vi.mocked(spawnSync).mockReturnValue({
      status: 0,
    } as ReturnType<typeof spawnSync>);
  });

  afterEach(() => {
    stdoutSpy.mockRestore();
    stderrSpy.mockRestore();
    vi.restoreAllMocks();
  });

  it("should return 0 when --help is passed", async () => {
    await expect(runGenerate(["--help"], tempDir)).resolves.toBe(0);
  });

  it("should return 1 when --out is missing", async () => {
    await expect(runGenerate(["sample.proto"], tempDir)).resolves.toBe(1);
  });

  it("should return 1 when proto files are missing", async () => {
    await expect(runGenerate(["-o", "out"], tempDir)).resolves.toBe(1);
  });

  it("should return 1 when proto file does not exist", async () => {
    vi.mocked(existsSync).mockReturnValue(false);
    await expect(
      runGenerate(["-o", "out", "missing.proto"], tempDir),
    ).resolves.toBe(1);
  });

  it("should invoke protoc when arguments are valid", async () => {
    const outDir = join(tempDir, "generated");
    await expect(
      runGenerate(["-o", outDir, "--no-webpb-proto", protoFile], tempDir),
    ).resolves.toBe(0);

    expect(mkdirSync).toHaveBeenCalledWith(outDir, { recursive: true });
    expect(spawnSync).toHaveBeenCalledWith(
      "/bin/protoc",
      expect.arrayContaining([
        `--plugin=protoc-gen-ts=/bin/plugin`,
        `--ts_out=${outDir}`,
        protoFile,
      ]),
      { stdio: "inherit" },
    );
  });

  it("should return 1 when protoc spawn fails", async () => {
    vi.mocked(spawnSync).mockReturnValue({
      error: new Error("spawn failed"),
    } as ReturnType<typeof spawnSync>);

    await expect(
      runGenerate(["-o", "out", "--no-webpb-proto", protoFile], tempDir),
    ).resolves.toBe(1);
  });

  it("should include bundled proto path by default", async () => {
    const outDir = join(tempDir, "generated");
    const protoDir = join(tempDir, "proto");
    mkdirSync(protoDir);
    vi.mocked(existsSync).mockImplementation((path) => {
      const value = String(path);
      return value === protoFile || value === protoDir;
    });

    await expect(
      runGenerate(["-o", outDir, protoFile], tempDir),
    ).resolves.toBe(0);

    expect(spawnSync).toHaveBeenCalledWith(
      "/bin/protoc",
      expect.arrayContaining(["-I", protoDir]),
      { stdio: "inherit" },
    );
  });

  it("should return protoc exit status when spawn succeeds", async () => {
    vi.mocked(spawnSync).mockReturnValue({
      status: 2,
    } as ReturnType<typeof spawnSync>);

    await expect(
      runGenerate(["-o", "out", "--no-webpb-proto", protoFile], tempDir),
    ).resolves.toBe(2);
  });

  it("should return 1 when protoc exits without status", async () => {
    vi.mocked(spawnSync).mockReturnValue({} as ReturnType<typeof spawnSync>);

    await expect(
      runGenerate(["-o", "out", "--no-webpb-proto", protoFile], tempDir),
    ).resolves.toBe(1);
  });

  it("should normalize windows paths when platform is win32", async () => {
    const platformSpy = vi.spyOn(process, "platform", "get").mockReturnValue("win32");
    const outDir = join(tempDir, "generated");
    const windowsProtoFile = protoFile.replace(/\//g, "\\");
    vi.mocked(existsSync).mockImplementation((path) => {
      const value = String(path);
      return value === protoFile || value === windowsProtoFile;
    });

    await expect(
      runGenerate(["-o", outDir, "--no-webpb-proto", windowsProtoFile], tempDir),
    ).resolves.toBe(0);

    expect(spawnSync).toHaveBeenCalledWith(
      "/bin/protoc",
      expect.arrayContaining([expect.stringMatching(/^--ts_out=.*generated/)]),
      { stdio: "inherit" },
    );
    platformSpy.mockRestore();
  });
});
