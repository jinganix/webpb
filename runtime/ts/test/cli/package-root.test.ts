import { mkdirSync, mkdtempSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { pathToFileURL } from "node:url";
import { afterEach, describe, expect, it } from "vitest";
import {
  bundledProtoInclude,
  findMonorepoRoot,
  monorepoPluginPath,
  packageRoot,
  readPackageVersion,
} from "../../src/cli/package-root";

describe("packageRoot", () => {
  it("should find nearest directory containing package.json", () => {
    const tempDir = mkdtempSync(join(tmpdir(), "webpb-root-"));
    writeFileSync(join(tempDir, "package.json"), "{}");
    const nested = join(tempDir, "nested");
    mkdirSync(nested);

    const moduleUrl = pathToFileURL(join(nested, "module.js")).href;
    expect(packageRoot(moduleUrl)).toBe(tempDir);
  });

  it("should throw when package.json is missing", () => {
    const tempDir = mkdtempSync(join(tmpdir(), "webpb-root-"));
    const moduleUrl = pathToFileURL(join(tempDir, "module.js")).href;
    expect(() => packageRoot(moduleUrl)).toThrow("webpb package root not found");
  });
});

describe("findMonorepoRoot", () => {
  let tempDir = "";

  afterEach(() => {
    tempDir = "";
  });

  it("should return repo root when plugin/go.mod exists", () => {
    tempDir = mkdtempSync(join(tmpdir(), "webpb-mono-"));
    mkdirSync(join(tempDir, "plugin"), { recursive: true });
    writeFileSync(join(tempDir, "plugin", "go.mod"), "module plugin\n");

    expect(findMonorepoRoot(join(tempDir, "runtime/ts/build"))).toBe(tempDir);
  });

  it("should return undefined when plugin/go.mod is missing", () => {
    tempDir = mkdtempSync(join(tmpdir(), "webpb-mono-"));
    expect(findMonorepoRoot(tempDir)).toBeUndefined();
  });
});

describe("bundledProtoInclude", () => {
  let tempDir = "";

  afterEach(() => {
    tempDir = "";
  });

  it("should prefer packaged proto directory when present", () => {
    tempDir = mkdtempSync(join(tmpdir(), "webpb-proto-"));
    const protoDir = join(tempDir, "proto");
    mkdirSync(protoDir);
    writeFileSync(join(protoDir, ".keep"), "");

    expect(bundledProtoInclude(tempDir)).toBe(protoDir);
  });

  it("should fall back to monorepo proto resources", () => {
    tempDir = mkdtempSync(join(tmpdir(), "webpb-proto-"));
    mkdirSync(join(tempDir, "plugin"), { recursive: true });
    writeFileSync(join(tempDir, "plugin", "go.mod"), "module plugin\n");
    const resources = join(tempDir, "lib/proto/src/main/resources");
    mkdirSync(resources, { recursive: true });

    expect(bundledProtoInclude(join(tempDir, "runtime/ts/build"))).toBe(resources);
  });

  it("should throw when proto includes are missing", () => {
    tempDir = mkdtempSync(join(tmpdir(), "webpb-proto-"));
    expect(() => bundledProtoInclude(tempDir)).toThrow(
      "webpb proto includes not found",
    );
  });
});

describe("monorepoPluginPath", () => {
  it("should return plugin binary path when monorepo exists", () => {
    const tempDir = mkdtempSync(join(tmpdir(), "webpb-plugin-"));
    mkdirSync(join(tempDir, "plugin/bin"), { recursive: true });
    writeFileSync(join(tempDir, "plugin", "go.mod"), "module plugin\n");

    const pluginPath = monorepoPluginPath(join(tempDir, "runtime/ts/build"));
    expect(pluginPath).toMatch(/plugin[/\\]bin[/\\]webpb-protoc-ts(\.exe)?$/);
  });

  it("should return undefined when monorepo is absent", () => {
    const tempDir = mkdtempSync(join(tmpdir(), "webpb-plugin-"));
    expect(monorepoPluginPath(tempDir)).toBeUndefined();
  });
});

describe("readPackageVersion", () => {
  it("should read version from package.json", () => {
    const tempDir = mkdtempSync(join(tmpdir(), "webpb-pkg-"));
    writeFileSync(
      join(tempDir, "package.json"),
      JSON.stringify({ version: "9.9.9" }),
    );

    expect(readPackageVersion(tempDir)).toBe("9.9.9");
  });
});
