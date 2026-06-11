import { describe, expect, it } from "vitest";

import { packageRootFromModuleUrl, protocPath } from "../../src/lib/paths.js";

describe("protocPath", () => {
  it.each([
    {
      expected: "a/b/c.proto",
      input: "a/b/c.proto",
      platform: "linux" as const,
    },
    {
      expected: "a/b/c.proto",
      input: "a\\b\\c.proto",
      platform: "win32" as const,
    },
  ])(
    "should return $expected when platform is $platform",
    ({ expected, input, platform }) => {
      expect(protocPath(input, platform)).toBe(expected);
    },
  );
});

describe("packageRootFromModuleUrl", () => {
  it("should resolve package root when file is two levels below it", () => {
    // Given
    const moduleUrl =
      "file:///repo/plugin/npm/webpb-protoc-ts/dist/bin/webpb-protoc-ts.js";

    // When
    const root = packageRootFromModuleUrl(moduleUrl, 2);

    // Then
    expect(root).toBe("/repo/plugin/npm/webpb-protoc-ts");
  });
});
