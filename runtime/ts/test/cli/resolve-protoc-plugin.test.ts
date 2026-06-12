import { spawnSync } from "node:child_process";
import {
  chmodSync,
  createWriteStream,
  existsSync,
  mkdirSync,
  mkdtempSync,
  writeFileSync,
} from "node:fs";
import { tmpdir } from "node:os";
import { dirname, join, normalize } from "node:path";
import { Readable } from "node:stream";
import { pipeline } from "node:stream/promises";
import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";
import {
  buildFromSource,
  cachePluginPath,
  downloadReleaseBinary,
  findPluginDir,
  resolvePluginAfterFailedDownload,
  resolveProtocPlugin,
} from "../../src/cli/resolve-protoc-plugin";
import * as packageRoot from "../../src/cli/package-root";

const testHomeDir = mkdtempSync(join(tmpdir(), "webpb-test-home-"));

vi.mock("node:child_process", () => ({
  spawnSync: vi.fn(),
}));

vi.mock("node:fs", async (importOriginal) => {
  const actual = await importOriginal<typeof import("node:fs")>();
  return {
    ...actual,
    chmodSync: vi.fn(),
    createWriteStream: vi.fn(),
    existsSync: vi.fn(),
  };
});

vi.mock("node:os", async (importOriginal) => {
  const actual = await importOriginal<typeof import("node:os")>();
  return {
    ...actual,
    homedir: vi.fn(() => testHomeDir),
  };
});

vi.mock("node:stream/promises", () => ({
  pipeline: vi.fn(),
}));

describe("cachePluginPath", () => {
  it("should include webpb version and platform in cache path", () => {
    expect(cachePluginPath("1.0.0")).toContain(
      join(testHomeDir, ".cache", "webpb", "1.0.0"),
    );
  });
});

describe("downloadReleaseBinary", () => {
  beforeEach(() => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        body: Readable.from(["binary"]),
        ok: true,
      }),
    );
    vi.mocked(createWriteStream).mockReturnValue({} as ReturnType<
      typeof createWriteStream
    >);
    vi.mocked(pipeline).mockResolvedValue(undefined);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("should download plugin binary when response is ok", async () => {
    await downloadReleaseBinary("1.0.0", "/tmp/plugin", "linux");

    expect(fetch).toHaveBeenCalled();
    expect(pipeline).toHaveBeenCalled();
    expect(chmodSync).toHaveBeenCalled();
  });

  it("should throw when response is not ok", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: false,
        status: 404,
      }),
    );

    await expect(
      downloadReleaseBinary("1.0.0", "/tmp/plugin"),
    ).rejects.toThrow("HTTP 404");
  });

  it("should throw when response body is empty", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
      }),
    );

    await expect(
      downloadReleaseBinary("1.0.0", "/tmp/plugin"),
    ).rejects.toThrow("empty response body");
  });
});

describe("findPluginDir", () => {
  const repoRoot = join("/repo");
  const pluginGoMod = join(repoRoot, "plugin", "go.mod");
  const pluginDir = join(repoRoot, "plugin");
  const runtimeTs = join(repoRoot, "runtime", "ts");

  it("should return plugin directory when plugin/go.mod exists", () => {
    vi.mocked(existsSync).mockImplementation(
      (path) => normalize(String(path)) === normalize(pluginGoMod),
    );
    expect(findPluginDir(runtimeTs)).toBe(pluginDir);
  });

  it("should return undefined when plugin/go.mod is missing", () => {
    vi.mocked(existsSync).mockReturnValue(false);
    expect(findPluginDir("/tmp/root")).toBeUndefined();
  });
});

describe("buildFromSource", () => {
  const repoRoot = join("/repo");
  const pluginGoMod = join(repoRoot, "plugin", "go.mod");
  const runtimeTs = join(repoRoot, "runtime", "ts");

  it("should return without spawning go when plugin dir is missing", () => {
    vi.mocked(existsSync).mockReturnValue(false);
    buildFromSource("/tmp/root", vi.fn() as never);
    expect(spawnSync).not.toHaveBeenCalled();
  });

  it("should spawn go build when plugin dir exists", () => {
    vi.mocked(existsSync).mockImplementation(
      (path) => normalize(String(path)) === normalize(pluginGoMod),
    );
    vi.mocked(spawnSync).mockReturnValue({
      status: 0,
    } as ReturnType<typeof spawnSync>);

    buildFromSource(runtimeTs, vi.fn() as never);
    expect(spawnSync).toHaveBeenCalled();
  });

  it("should call exit when go build fails", () => {
    vi.mocked(existsSync).mockImplementation(
      (path) => normalize(String(path)) === normalize(pluginGoMod),
    );
    vi.mocked(spawnSync).mockReturnValue({
      status: 2,
    } as ReturnType<typeof spawnSync>);
    const exit = vi.fn((code: number) => {
      throw new Error(`exit:${code}`);
    });

    expect(() => buildFromSource(runtimeTs, exit as never)).toThrow("exit:2");
  });
});

describe("resolvePluginAfterFailedDownload", () => {
  const repoRoot = join("/repo");
  const pluginGoMod = join(repoRoot, "plugin", "go.mod");
  const runtimeTs = join(repoRoot, "runtime", "ts");
  const localPlugin = join("/local", "plugin");

  it("should return rebuilt plugin path when source build succeeds", () => {
    vi.spyOn(packageRoot, "monorepoPluginPath").mockReturnValue(localPlugin);
    vi.mocked(existsSync).mockImplementation((path) => {
      const value = normalize(String(path));
      return (
        value === normalize(pluginGoMod) || value === normalize(localPlugin)
      );
    });
    vi.mocked(spawnSync).mockReturnValue({
      status: 0,
    } as ReturnType<typeof spawnSync>);

    const rebuilt = resolvePluginAfterFailedDownload(
      runtimeTs,
      vi.fn() as never,
    );
    expect(rebuilt).toBe(localPlugin);
  });

  it("should return undefined when plugin directory is missing", () => {
    vi.mocked(existsSync).mockReturnValue(false);
    expect(
      resolvePluginAfterFailedDownload(runtimeTs, vi.fn() as never),
    ).toBeUndefined();
  });
});

describe("resolveProtocPlugin", () => {
  let tempRoot = "";
  const originalPlugin = process.env.WEBPB_PROTOC_TS;
  const originalVersion = process.env.WEBPB_VERSION;

  beforeEach(() => {
    tempRoot = mkdtempSync(join(tmpdir(), "webpb-resolve-plugin-"));
    writeFileSync(
      join(tempRoot, "package.json"),
      JSON.stringify({ version: "2.0.0" }),
    );
    vi.mocked(existsSync).mockReturnValue(false);
  });

  afterEach(() => {
    if (originalPlugin === undefined) {
      delete process.env.WEBPB_PROTOC_TS;
    } else {
      process.env.WEBPB_PROTOC_TS = originalPlugin;
    }
    if (originalVersion === undefined) {
      delete process.env.WEBPB_VERSION;
    } else {
      process.env.WEBPB_VERSION = originalVersion;
    }
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it("should return explicit plugin path when option is set", async () => {
    await expect(
      resolveProtocPlugin(tempRoot, { pluginPath: "/custom/plugin" }),
    ).resolves.toBe("/custom/plugin");
  });

  it("should return WEBPB_PROTOC_TS when environment variable is set", async () => {
    process.env.WEBPB_PROTOC_TS = "/env/plugin";
    await expect(resolveProtocPlugin(tempRoot)).resolves.toBe("/env/plugin");
  });

  it("should return cached plugin when cache file exists", async () => {
    const cached = cachePluginPath("2.0.0");
    vi.mocked(existsSync).mockImplementation((path) => String(path) === cached);

    await expect(resolveProtocPlugin(tempRoot)).resolves.toBe(cached);
  });

  it("should download plugin when cache is missing", async () => {
    const cached = cachePluginPath("2.0.0");
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        body: Readable.from(["binary"]),
        ok: true,
      }),
    );
    vi.mocked(createWriteStream).mockReturnValue({} as ReturnType<
      typeof createWriteStream
    >);
    vi.mocked(pipeline).mockResolvedValue(undefined);

    await expect(resolveProtocPlugin(tempRoot)).resolves.toBe(cached);
  });

  it("should call exit when plugin cannot be resolved", async () => {
    const exit = vi.fn((code: number) => {
      throw new Error(`exit:${code}`);
    });
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: false,
        status: 404,
      }),
    );

    await expect(
      resolveProtocPlugin(tempRoot, { exit: exit as never }),
    ).rejects.toThrow("exit:1");
    expect(exit).toHaveBeenCalledWith(1);
  });

  it("should call process.exit when plugin cannot be resolved without custom exit", async () => {
    const exitSpy = vi.spyOn(process, "exit").mockImplementation(((code) => {
      throw new Error(`exit:${code}`);
    }) as typeof process.exit);
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: false,
        status: 404,
      }),
    );
    vi.mocked(existsSync).mockReturnValue(false);

    await expect(resolveProtocPlugin(tempRoot)).rejects.toThrow("exit:1");
    expect(exitSpy).toHaveBeenCalledWith(1);
    exitSpy.mockRestore();
  });

  it("should return local monorepo plugin when binary exists", async () => {
    vi.spyOn(packageRoot, "monorepoPluginPath").mockReturnValue("/local/plugin");
    vi.mocked(existsSync).mockImplementation((path) => String(path) === "/local/plugin");

    await expect(resolveProtocPlugin(tempRoot)).resolves.toBe("/local/plugin");
  });

  it("should return rebuilt plugin when download fails and source build succeeds", async () => {
    const repoRoot = join(tempRoot, "repo");
    const runtimeTs = join(repoRoot, "runtime", "ts");
    const pluginGoMod = join(repoRoot, "plugin", "go.mod");
    const rebuiltPlugin = join(repoRoot, "plugin", "bin", "webpb-protoc-ts");
    mkdirSync(runtimeTs, { recursive: true });
    writeFileSync(
      join(runtimeTs, "package.json"),
      JSON.stringify({ version: "2.0.0" }),
    );
    mkdirSync(dirname(pluginGoMod), { recursive: true });
    writeFileSync(pluginGoMod, "module plugin\n");
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: false,
        status: 404,
      }),
    );
    vi.spyOn(packageRoot, "monorepoPluginPath").mockReturnValue(rebuiltPlugin);
    vi.mocked(existsSync).mockImplementation((path) => {
      const value = normalize(String(path));
      return (
        value === normalize(pluginGoMod) || value === normalize(rebuiltPlugin)
      );
    });
    vi.mocked(spawnSync).mockReturnValue({
      status: 0,
    } as ReturnType<typeof spawnSync>);

    await expect(resolveProtocPlugin(runtimeTs)).resolves.toBe(rebuiltPlugin);
  });

  it("should use webpbVersion option when resolving download version", async () => {
    process.env.WEBPB_VERSION = "9.8.7";
    const cached = cachePluginPath("9.8.7");
    vi.mocked(existsSync).mockImplementation((path) => String(path) === cached);

    await expect(resolveProtocPlugin(tempRoot)).resolves.toBe(cached);
  });

  it("should use webpbVersion option when resolving download version", async () => {
    const cached = cachePluginPath("3.2.1");
    vi.mocked(existsSync).mockImplementation((path) => String(path) === cached);

    await expect(
      resolveProtocPlugin(tempRoot, { webpbVersion: "3.2.1" }),
    ).resolves.toBe(cached);
  });

  it("should skip chmod on windows when downloading plugin", async () => {
    vi.mocked(chmodSync).mockClear();
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        body: Readable.from(["binary"]),
        ok: true,
      }),
    );
    vi.mocked(createWriteStream).mockReturnValue({} as ReturnType<
      typeof createWriteStream
    >);
    vi.mocked(pipeline).mockResolvedValue(undefined);

    await downloadReleaseBinary("1.0.0", "/tmp/plugin.exe", "win32");
    expect(chmodSync).not.toHaveBeenCalled();
  });
});
