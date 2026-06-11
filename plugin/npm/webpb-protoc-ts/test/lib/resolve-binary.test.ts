import { describe, expect, it, vi } from "vitest";

import {
  monorepoBinaryPath,
  resolvePluginBinary,
  runPluginBinary,
  vendorBinaryPath,
} from "../../src/lib/resolve-binary.js";

const packageDir = "/repo/plugin/npm/webpb-protoc-ts";

describe("vendorBinaryPath", () => {
  it("should append .exe on windows", () => {
    expect(vendorBinaryPath(packageDir, "win32", "x64")).toBe(
      "/repo/plugin/npm/webpb-protoc-ts/vendor/win32-x64/webpb-protoc-ts.exe",
    );
  });

  it("should normalize amd64 arch to x64", () => {
    expect(vendorBinaryPath(packageDir, "linux", "amd64")).toBe(
      "/repo/plugin/npm/webpb-protoc-ts/vendor/linux-x64/webpb-protoc-ts",
    );
  });
});

describe("monorepoBinaryPath", () => {
  it("should point at plugin/bin under the monorepo", () => {
    expect(monorepoBinaryPath(packageDir, "linux")).toBe(
      "/repo/plugin/bin/webpb-protoc-ts",
    );
  });
});

describe("resolvePluginBinary", () => {
  it("should prefer vendor binary when it exists", () => {
    // Given
    const vendor = vendorBinaryPath(packageDir, "darwin", "arm64");
    const exists = vi.fn((path: string) => path === vendor);

    // When
    const resolved = resolvePluginBinary(packageDir, exists, "darwin", "arm64");

    // Then
    expect(resolved).toEqual({ path: vendor, source: "vendor" });
  });

  it("should use monorepo binary when vendor is missing", () => {
    // Given
    const monorepo = monorepoBinaryPath(packageDir, "darwin");
    const exists = vi.fn((path: string) => path === monorepo);

    // When
    const resolved = resolvePluginBinary(packageDir, exists, "darwin", "arm64");

    // Then
    expect(resolved).toEqual({ path: monorepo, source: "monorepo" });
  });

  it("should return null when no binary exists", () => {
    expect(
      resolvePluginBinary(packageDir, () => false, "darwin", "arm64"),
    ).toBeNull();
  });
});

describe("runPluginBinary", () => {
  it("should return spawn status when process exits normally", () => {
    const spawn = vi.fn(() => ({ status: 0 }));
    expect(runPluginBinary("/bin/plugin", ["--help"], spawn)).toBe(0);
    expect(spawn).toHaveBeenCalledWith("/bin/plugin", ["--help"], {
      stdio: "inherit",
    });
  });

  it("should return 1 when spawn status is null", () => {
    const spawn = vi.fn(() => ({ status: null }));
    expect(runPluginBinary("/bin/plugin", [], spawn)).toBe(1);
  });
});
