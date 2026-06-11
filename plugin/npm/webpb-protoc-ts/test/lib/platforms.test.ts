import { describe, expect, it } from "vitest";

import {
  hostPlatform,
  normalizeNodeArch,
  PLATFORMS,
} from "../../src/lib/platforms.js";

describe("normalizeNodeArch", () => {
  it.each([
    { arch: "amd64", expected: "x64" },
    { arch: "arm64", expected: "arm64" },
  ])("should return $expected when arch is $arch", ({ arch, expected }) => {
    expect(normalizeNodeArch(arch)).toBe(expected);
  });
});

describe("hostPlatform", () => {
  it("should return matching platform when os and arch are known", () => {
    expect(hostPlatform("darwin", "arm64")).toEqual(PLATFORMS[0]);
  });

  it("should fall back to linux x64 when platform is unknown", () => {
    expect(hostPlatform("freebsd", "mips")).toEqual(PLATFORMS[2]);
  });
});
