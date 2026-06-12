import { spawnSync } from "node:child_process";
import { existsSync, mkdirSync } from "node:fs";
import { bundledProtoInclude, packageRoot } from "./package-root.js";
import { GENERATE_HELP, parseGenerateArgs } from "./parse-args.js";
import { resolveProtocPlugin } from "./resolve-protoc-plugin.js";
import { resolveProtoc } from "./resolve-protoc.js";

function protocPath(filePath: string): string {
  return process.platform === "win32" ? filePath.replace(/\\/g, "/") : filePath;
}

export async function runGenerate(
  argv: string[],
  rootOverride?: string,
): Promise<number> {
  const parsed = parseGenerateArgs(argv);
  if (parsed.help) {
    process.stdout.write(GENERATE_HELP);
    return 0;
  }
  if (!parsed.out) {
    console.error("Missing required option: --out");
    process.stdout.write(GENERATE_HELP);
    return 1;
  }
  if (parsed.protoFiles.length === 0) {
    console.error("No .proto files specified");
    process.stdout.write(GENERATE_HELP);
    return 1;
  }

  for (const protoFile of parsed.protoFiles) {
    if (!existsSync(protoFile)) {
      console.error(`Proto file not found: ${protoFile}`);
      return 1;
    }
  }

  const root = rootOverride ?? packageRoot(import.meta.url);
  const includeDirs = [...parsed.includes];
  if (parsed.webpbProto) {
    includeDirs.unshift(bundledProtoInclude(root));
  }

  mkdirSync(parsed.out, { recursive: true });

  const protoc = await resolveProtoc(root, {
    protobufVersion: parsed.protobufVersion,
    protocPath: parsed.protoc,
  });
  const pluginBin = await resolveProtocPlugin(root, {
    pluginPath: parsed.plugin,
    webpbVersion: parsed.webpbVersion,
  });

  const args = [
    ...includeDirs.flatMap((dir) => ["-I", protocPath(dir)]),
    `--plugin=protoc-gen-ts=${protocPath(pluginBin)}`,
    `--ts_out=${protocPath(parsed.out)}`,
    ...parsed.protoFiles.map(protocPath),
  ];

  const result = spawnSync(protoc, args, { stdio: "inherit" });
  if (result.error) {
    console.error(
      `Failed to run ${protoc}. Install protoc, set PROTOC, or retry to auto-download.`,
    );
    return 1;
  }
  return result.status ?? 1;
}
