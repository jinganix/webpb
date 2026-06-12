import { describe, expect, it } from "vitest";
import {
  executableExtension,
  protocMavenClassifier,
  webpbReleasePlatform,
} from "../../src/cli/platform";

describe("webpbReleasePlatform", () => {
  it.each([
    { arch: "arm64" as const, expected: "darwin-arm64", platform: "darwin" as const },
    { arch: "x64" as const, expected: "darwin-amd64", platform: "darwin" as const },
    { arch: "arm64" as const, expected: "linux-arm64", platform: "linux" as const },
    { arch: "x64" as const, expected: "linux-amd64", platform: "linux" as const },
    { arch: "ia32" as const, expected: "windows-386", platform: "win32" as const },
    { arch: "x64" as const, expected: "windows-amd64", platform: "win32" as const },
  ])(
    "should return $expected when platform is $platform and arch is $arch",
    ({ platform, arch, expected }) => {
      expect(webpbReleasePlatform(platform, arch)).toBe(expected);
    },
  );

  it("should throw when platform is unsupported", () => {
    expect(() => webpbReleasePlatform("aix", "x64")).toThrow(
      "Unsupported platform: aix x64",
    );
  });
});

describe("protocMavenClassifier", () => {
  it.each([
    { arch: "arm64" as const, expected: "osx-aarch_64", platform: "darwin" as const },
    { arch: "x64" as const, expected: "osx-x86_64", platform: "darwin" as const },
    { arch: "arm64" as const, expected: "linux-aarch_64", platform: "linux" as const },
    { arch: "ppc64" as const, expected: "linux-ppcle_64", platform: "linux" as const },
    { arch: "s390x" as const, expected: "linux-s390_64", platform: "linux" as const },
    { arch: "ia32" as const, expected: "linux-x86_32", platform: "linux" as const },
    { arch: "x64" as const, expected: "linux-x86_64", platform: "linux" as const },
    { arch: "ia32" as const, expected: "windows-x86_32", platform: "win32" as const },
    { arch: "x64" as const, expected: "windows-x86_64", platform: "win32" as const },
  ])(
    "should return $expected when platform is $platform and arch is $arch",
    ({ platform, arch, expected }) => {
      expect(protocMavenClassifier(platform, arch)).toBe(expected);
    },
  );

  it("should throw when platform is unsupported", () => {
    expect(() => protocMavenClassifier("freebsd", "x64")).toThrow(
      "Unsupported platform: freebsd x64",
    );
  });
});

describe("executableExtension", () => {
  it.each([
    { expected: ".exe", platform: "win32" as const },
    { expected: "", platform: "linux" as const },
    { expected: "", platform: "darwin" as const },
  ])(
    "should return $expected when platform is $platform",
    ({ platform, expected }) => {
      expect(executableExtension(platform)).toBe(expected);
    },
  );
});
