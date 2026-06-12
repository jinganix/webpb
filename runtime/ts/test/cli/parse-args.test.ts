import { describe, expect, it } from "vitest";
import { parseGenerateArgs } from "../../src/cli/parse-args";

describe("parseGenerateArgs", () => {
  it("should return help when --help is passed", () => {
    const parsed = parseGenerateArgs(["--help"]);
    expect(parsed.help).toBe(true);
  });

  it("should parse output, includes, and proto files", () => {
    const parsed = parseGenerateArgs([
      "-o",
      "out",
      "-I",
      "inc",
      "--include",
      "inc2",
      "--no-webpb-proto",
      "--protoc",
      "/bin/protoc",
      "--plugin",
      "/bin/plugin",
      "--webpb-version",
      "1.0.0",
      "--protobuf-version",
      "4.0.0",
      "a.proto",
      "b.proto",
    ]);

    expect(parsed).toStrictEqual({
      includes: ["inc", "inc2"],
      out: "out",
      plugin: "/bin/plugin",
      protoFiles: ["a.proto", "b.proto"],
      protobufVersion: "4.0.0",
      protoc: "/bin/protoc",
      webpbProto: false,
      webpbVersion: "1.0.0",
    });
  });

  it("should collect proto files after -- separator", () => {
    const parsed = parseGenerateArgs(["-o", "out", "--", "a.proto", "b.proto"]);
    expect(parsed.protoFiles).toStrictEqual(["a.proto", "b.proto"]);
  });

  it("should throw when option is unknown", () => {
    expect(() => parseGenerateArgs(["--unknown"])).toThrow(
      "Unknown option: --unknown",
    );
  });
});
