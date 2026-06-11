import { describe, expect, it } from "vitest";

import {
  applyPackageVersion,
  formatPackageJson,
  parseGradleVersion,
} from "../../src/lib/sync-version.js";

describe("parseGradleVersion", () => {
  it("should return version without -SNAPSHOT suffix", () => {
    const content = "group = io.github.jinganix\nversion = 0.0.19-SNAPSHOT\n";
    expect(parseGradleVersion(content)).toBe("0.0.19");
  });

  it("should return null when version line is missing", () => {
    expect(parseGradleVersion("group = io.github.jinganix\n")).toBeNull();
  });
});

describe("applyPackageVersion", () => {
  it("should update version when it changed", () => {
    const pkg = { version: "0.0.1" };
    expect(applyPackageVersion(pkg, "0.0.2")).toBe(true);
    expect(pkg.version).toBe("0.0.2");
  });

  it("should return false when version is unchanged", () => {
    const pkg = { version: "0.0.2" };
    expect(applyPackageVersion(pkg, "0.0.2")).toBe(false);
  });
});

describe("formatPackageJson", () => {
  it("should append trailing newline", () => {
    expect(formatPackageJson({ version: "1.0.0" })).toBe(
      '{\n  "version": "1.0.0"\n}\n',
    );
  });
});
