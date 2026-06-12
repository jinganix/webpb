export const GENERATE_HELP = `Usage: webpb generate [options] -- <files.proto...>

Generate TypeScript from protobuf definitions using the webpb protoc plugin.

Options:
  -o, --out <dir>              Output directory (--ts_out), required
  -I, --include <dir>          Additional proto include path (repeatable)
      --no-webpb-proto         Skip bundled webpb/WebpbExtend.proto includes
      --protoc <path>          Path to protoc (default: PATH, then auto-download)
      --plugin <path>          Path to webpb-protoc-ts (default: auto-download)
      --webpb-version <ver>    webpb release version for plugin download
      --protobuf-version <ver> protoc version for auto-download (default: bundled)
  -h, --help                   Show this help

Environment variables:
  PROTOC                       Path to protoc
  WEBPB_PROTOC_TS              Path to webpb-protoc-ts
  WEBPB_VERSION                webpb release version for plugin download
  WEBPB_PROTOBUF_VERSION       protoc version for auto-download

Examples:
  webpb generate -o src/generated -I protos protos/*.proto
  webpb generate --out generated/proto --include ../proto src/*.proto
`;

export interface GenerateArgs {
  help?: boolean;
  out?: string;
  includes: string[];
  webpbProto: boolean;
  protoc?: string;
  plugin?: string;
  webpbVersion?: string;
  protobufVersion?: string;
  protoFiles: string[];
}

export function parseGenerateArgs(argv: string[]): GenerateArgs {
  const result: GenerateArgs = {
    includes: [],
    protoFiles: [],
    webpbProto: true,
  };

  let index = 0;
  while (index < argv.length) {
    const arg = argv[index];
    if (arg === "--") {
      result.protoFiles.push(...argv.slice(index + 1));
      break;
    }
    switch (arg) {
      case "-o":
      case "--out":
        result.out = argv[++index];
        break;
      case "-I":
      case "--include":
        result.includes.push(argv[++index]);
        break;
      case "--no-webpb-proto":
        result.webpbProto = false;
        break;
      case "--protoc":
        result.protoc = argv[++index];
        break;
      case "--plugin":
        result.plugin = argv[++index];
        break;
      case "--webpb-version":
        result.webpbVersion = argv[++index];
        break;
      case "--protobuf-version":
        result.protobufVersion = argv[++index];
        break;
      case "-h":
      case "--help":
        return { help: true, includes: [], protoFiles: [], webpbProto: true };
      default:
        if (arg.startsWith("-")) {
          throw new Error(`Unknown option: ${arg}`);
        }
        result.protoFiles.push(arg);
        break;
    }
    index += 1;
  }

  return result;
}
