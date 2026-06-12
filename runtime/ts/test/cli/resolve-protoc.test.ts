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
import { dirname, join } from "node:path";
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
import * as resolveProtocModule from "../../src/cli/resolve-protoc";
import {
  cacheProtocPath,
  downloadProtocFromMaven,
  resolveOnPath,
  resolveProtoc,
} from "../../src/cli/resolve-protoc";

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

describe("cacheProtocPath", () => {
  it("should build cache path from protobuf version", () => {
    expect(cacheProtocPath("4.35.0")).toContain(
      join(testHomeDir, ".cache", "webpb", "protoc", "4.35.0"),
    );
  });
});

describe("resolveOnPath", () => {
  it("should return undefined when command is undefined", () => {
    expect(resolveOnPath(undefined)).toBeUndefined();
  });

  it("should return first resolved path when lookup succeeds", () => {
    vi.mocked(spawnSync).mockReturnValue({
      status: 0,
      stdout: "/usr/bin/protoc\n",
    } as ReturnType<typeof spawnSync>);

    expect(resolveOnPath("protoc")).toBe("/usr/bin/protoc");
  });

  it("should return undefined when lookup fails", () => {
    vi.mocked(spawnSync).mockReturnValue({
      status: 1,
      stdout: "",
    } as ReturnType<typeof spawnSync>);

    expect(resolveOnPath("protoc")).toBeUndefined();
  });

  it("should return undefined when lookup stdout is empty", () => {
    vi.mocked(spawnSync).mockReturnValue({
      status: 0,
      stdout: "",
    } as ReturnType<typeof spawnSync>);

    expect(resolveOnPath("protoc")).toBeUndefined();
  });

  it("should use where on windows when lookup succeeds", () => {
    const platformSpy = vi
      .spyOn(process, "platform", "get")
      .mockReturnValue("win32");
    vi.mocked(spawnSync).mockReturnValue({
      status: 0,
      stdout: "C:\\tools\\protoc.exe\r\n",
    } as ReturnType<typeof spawnSync>);

    expect(resolveOnPath("protoc")).toBe("C:\\tools\\protoc.exe");
    expect(spawnSync).toHaveBeenCalledWith("where", ["protoc"], {
      encoding: "utf8",
    });
    platformSpy.mockRestore();
  });
});

describe("downloadProtocFromMaven", () => {
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

  it("should download protoc binary when response is ok", async () => {
    await downloadProtocFromMaven("4.35.0", "/tmp/protoc", "linux");

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
      downloadProtocFromMaven("4.35.0", "/tmp/protoc"),
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
      downloadProtocFromMaven("4.35.0", "/tmp/protoc"),
    ).rejects.toThrow("empty response body");
  });
});

describe("resolveProtoc", () => {
  let tempRoot = "";
  const originalProtoc = process.env.PROTOC;
  const originalProtobufVersion = process.env.WEBPB_PROTOBUF_VERSION;

  beforeEach(() => {
    tempRoot = mkdtempSync(join(tmpdir(), "webpb-resolve-protoc-"));
    writeFileSync(
      join(tempRoot, "webpb-version.properties"),
      "webpbVersion=1.0.0\nprotobufVersion=4.35.0\n",
    );
    vi.mocked(existsSync).mockReturnValue(false);
    vi.mocked(spawnSync).mockReturnValue({
      status: 1,
      stdout: "",
    } as ReturnType<typeof spawnSync>);
  });

  afterEach(() => {
    if (originalProtoc === undefined) {
      delete process.env.PROTOC;
    } else {
      process.env.PROTOC = originalProtoc;
    }
    if (originalProtobufVersion === undefined) {
      delete process.env.WEBPB_PROTOBUF_VERSION;
    } else {
      process.env.WEBPB_PROTOBUF_VERSION = originalProtobufVersion;
    }
    vi.unstubAllGlobals();
  });

  it("should return explicit protoc path when option is set", async () => {
    await expect(
      resolveProtoc(tempRoot, { protocPath: "/custom/protoc" }),
    ).resolves.toBe("/custom/protoc");
  });

  it("should return PROTOC environment variable when set", async () => {
    process.env.PROTOC = "/env/protoc";
    await expect(resolveProtoc(tempRoot)).resolves.toBe("/env/protoc");
  });

  it("should return protoc from PATH when lookup succeeds", async () => {
    vi.mocked(spawnSync).mockReturnValue({
      status: 0,
      stdout: "/usr/bin/protoc\n",
    } as ReturnType<typeof spawnSync>);

    await expect(resolveProtoc(tempRoot)).resolves.toBe("/usr/bin/protoc");
  });

  it("should return cached protoc when cache file exists", async () => {
    const cached = cacheProtocPath("4.35.0");
    vi.mocked(existsSync).mockImplementation((path) => String(path) === cached);

    await expect(resolveProtoc(tempRoot)).resolves.toBe(cached);
  });

  it("should download protoc when cache is missing", async () => {
    const cached = cacheProtocPath("4.35.0");
    vi.spyOn(resolveProtocModule, "downloadProtocFromMaven").mockResolvedValue(
      undefined,
    );

    await expect(resolveProtoc(tempRoot)).resolves.toBe(cached);
  });

  it("should call exit when download fails", async () => {
    const exit = vi.fn((code: number) => {
      throw new Error(`exit:${code}`);
    });
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: false,
        status: 500,
      }),
    );

    await expect(
      resolveProtoc(tempRoot, { exit: exit as never }),
    ).rejects.toThrow("exit:1");
    expect(exit).toHaveBeenCalledWith(1);
  });

  it("should use WEBPB_PROTOBUF_VERSION when set", async () => {
    process.env.WEBPB_PROTOBUF_VERSION = "4.99.0";
    const cached = cacheProtocPath("4.99.0");
    vi.mocked(existsSync).mockImplementation((path) => String(path) === cached);

    await expect(resolveProtoc(tempRoot)).resolves.toBe(cached);
  });

  it("should call process.exit when download fails without custom exit handler", async () => {
    const exitSpy = vi.spyOn(process, "exit").mockImplementation(((code) => {
      throw new Error(`exit:${code}`);
    }) as typeof process.exit);
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: false,
        status: 500,
      }),
    );

    await expect(resolveProtoc(tempRoot)).rejects.toThrow("exit:1");
    expect(exitSpy).toHaveBeenCalledWith(1);
    exitSpy.mockRestore();
  });

  it("should skip chmod on windows when downloading protoc", async () => {
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

    await downloadProtocFromMaven("4.35.0", "/tmp/protoc.exe", "win32");
    expect(chmodSync).not.toHaveBeenCalled();
  });
});
