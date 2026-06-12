import { mkdirSync, mkdtempSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { afterEach, describe, expect, it } from "vitest";
import { readBundledVersions } from "../../src/cli/versions";

describe("readBundledVersions", () => {
  let tempDir = "";

  afterEach(() => {
    tempDir = "";
  });

  it("should read webpb and protobuf versions when properties file is valid", () => {
    tempDir = mkdtempSync(join(tmpdir(), "webpb-versions-"));
    writeFileSync(
      join(tempDir, "webpb-version.properties"),
      "webpbVersion=1.2.3\nprotobufVersion=4.35.0\n",
    );

    expect(readBundledVersions(tempDir)).toStrictEqual({
      protobufVersion: "4.35.0",
      webpbVersion: "1.2.3",
    });
  });

  it("should throw when properties file is invalid", () => {
    tempDir = mkdtempSync(join(tmpdir(), "webpb-versions-"));
    writeFileSync(join(tempDir, "webpb-version.properties"), "webpbVersion=\n");

    expect(() => readBundledVersions(tempDir)).toThrow(
      /Invalid webpb-version.properties/,
    );
  });

  it("should ignore blank lines and comments", () => {
    tempDir = mkdtempSync(join(tmpdir(), "webpb-versions-"));
    mkdirSync(tempDir, { recursive: true });
    writeFileSync(
      join(tempDir, "webpb-version.properties"),
      "# comment\n\nwebpbVersion=0.0.1\nprotobufVersion=4.0.0\n",
    );

    expect(readBundledVersions(tempDir)).toStrictEqual({
      protobufVersion: "4.0.0",
      webpbVersion: "0.0.1",
    });
  });
});
