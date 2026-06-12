import { spawnSync } from "node:child_process";
import { chmodSync, createWriteStream, existsSync, mkdirSync } from "node:fs";
import { homedir } from "node:os";
import { dirname, join } from "node:path";
import { pipeline } from "node:stream/promises";
import { executableExtension, protocMavenClassifier } from "./platform.js";
import { readBundledVersions } from "./versions.js";

export type ExitFn = (code: number) => never;

export interface ResolveProtocOptions {
  protocPath?: string;
  protobufVersion?: string;
  exit?: ExitFn;
}

const defaultExit: ExitFn = (code) => {
  process.exit(code);
};

export function cacheProtocPath(protobufVersion: string): string {
  const ext = executableExtension();
  return join(
    homedir(),
    ".cache",
    "webpb",
    "protoc",
    protobufVersion,
    `protoc${ext}`,
  );
}

export function resolveOnPath(command: string | undefined): string | undefined {
  if (!command) {
    return undefined;
  }
  const lookup = process.platform === "win32" ? "where" : "which";
  const result = spawnSync(lookup, [command], { encoding: "utf8" });
  if (result.status !== 0) {
    return undefined;
  }
  const resolved = result.stdout.trim().split(/\r?\n/)[0];
  return resolved || undefined;
}

export async function downloadProtocFromMaven(
  protobufVersion: string,
  destination: string,
  platform: NodeJS.Platform = process.platform,
): Promise<void> {
  const classifier = protocMavenClassifier();
  const fileName = `protoc-${protobufVersion}-${classifier}.exe`;
  const url = `https://repo1.maven.org/maven2/com/google/protobuf/protoc/${protobufVersion}/${fileName}`;
  mkdirSync(dirname(destination), { recursive: true });
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`Failed to download ${url}: HTTP ${response.status}`);
  }
  if (!response.body) {
    throw new Error(`Failed to download ${url}: empty response body`);
  }
  await pipeline(response.body, createWriteStream(destination));
  if (platform !== "win32") {
    chmodSync(destination, 0o755);
  }
}

export async function resolveProtoc(
  root: string,
  options: ResolveProtocOptions = {},
): Promise<string> {
  const exit = options.exit ?? defaultExit;

  if (options.protocPath) {
    return options.protocPath;
  }
  if (process.env.PROTOC) {
    return process.env.PROTOC;
  }

  const onPath = resolveOnPath("protoc");
  if (onPath) {
    return onPath;
  }

  const bundled = readBundledVersions(root);
  const protobufVersion =
    options.protobufVersion ??
    process.env.WEBPB_PROTOBUF_VERSION ??
    bundled.protobufVersion;
  const cachedPath = cacheProtocPath(protobufVersion);
  if (existsSync(cachedPath)) {
    return cachedPath;
  }

  try {
    console.log(
      `Downloading protoc ${protobufVersion} for ${protocMavenClassifier()}...`,
    );
    await downloadProtocFromMaven(protobufVersion, cachedPath);
    return cachedPath;
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    console.error(`Failed to download protoc: ${message}`);
    console.error(
      [
        "protoc not found.",
        "Install options:",
        "  - add protoc to PATH",
        "  - set PROTOC to the protoc binary path",
        "  - set WEBPB_PROTOBUF_VERSION and retry (auto-download from Maven Central)",
      ].join("\n"),
    );
    return exit(1);
  }
}
