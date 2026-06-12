import { spawnSync } from "node:child_process";
import { chmodSync, createWriteStream, existsSync, mkdirSync } from "node:fs";
import { homedir } from "node:os";
import { dirname, join } from "node:path";
import { pipeline } from "node:stream/promises";
import { monorepoPluginPath, readPackageVersion } from "./package-root.js";
import { executableExtension, webpbReleasePlatform } from "./platform.js";
import type { ExitFn } from "./resolve-protoc.js";

export interface ResolveProtocPluginOptions {
  pluginPath?: string;
  webpbVersion?: string;
  exit?: ExitFn;
}

const defaultExit: ExitFn = (code) => {
  process.exit(code);
};

function defaultWebpbVersion(
  root: string,
  versionOverride: string | undefined,
): string {
  if (process.env.WEBPB_VERSION) {
    return process.env.WEBPB_VERSION;
  }
  if (versionOverride) {
    return versionOverride;
  }
  return readPackageVersion(root);
}

export function cachePluginPath(version: string): string {
  const ext = executableExtension();
  return join(
    homedir(),
    ".cache",
    "webpb",
    version,
    `webpb-protoc-ts-${webpbReleasePlatform()}${ext}`,
  );
}

export async function downloadReleaseBinary(
  version: string,
  destination: string,
  platform: NodeJS.Platform = process.platform,
): Promise<void> {
  const ext = executableExtension(platform);
  const releasePlatform = webpbReleasePlatform(platform);
  const assetName = `webpb-protoc-ts-${releasePlatform}${ext}`;
  const url = `https://github.com/jinganix/webpb/releases/download/v${version}/${assetName}`;
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

export function findPluginDir(root: string): string | undefined {
  let dir = root;
  while (dir !== dirname(dir)) {
    if (existsSync(join(dir, "plugin", "go.mod"))) {
      return join(dir, "plugin");
    }
    dir = dirname(dir);
  }
  return undefined;
}

export function buildFromSource(root: string, exit: ExitFn): void {
  const ext = executableExtension();
  const binaryName = `webpb-protoc-ts${ext}`;
  const pluginDir = findPluginDir(root);
  if (!pluginDir) {
    return;
  }
  console.log("Building webpb-protoc-ts from source...");
  const result = spawnSync(
    "go",
    ["build", "-o", join("bin", binaryName), "./cmd/webpb-ts"],
    { cwd: pluginDir, stdio: "inherit" },
  );
  if (result.status !== 0) {
    exit(result.status ?? 1);
  }
}

export function resolvePluginAfterFailedDownload(
  root: string,
  exit: ExitFn,
): string | undefined {
  if (findPluginDir(root)) {
    buildFromSource(root, exit);
    const rebuiltPath = monorepoPluginPath(root);
    if (rebuiltPath && existsSync(rebuiltPath)) {
      return rebuiltPath;
    }
  }
  return undefined;
}

export async function resolveProtocPlugin(
  root: string,
  options: ResolveProtocPluginOptions = {},
): Promise<string> {
  const exit = options.exit ?? defaultExit;

  if (options.pluginPath) {
    return options.pluginPath;
  }
  if (process.env.WEBPB_PROTOC_TS) {
    return process.env.WEBPB_PROTOC_TS;
  }

  const localPath = monorepoPluginPath(root);
  if (localPath && existsSync(localPath)) {
    return localPath;
  }

  const version = defaultWebpbVersion(root, options.webpbVersion);
  const cachedPath = cachePluginPath(version);
  if (existsSync(cachedPath)) {
    return cachedPath;
  }

  try {
    console.log(
      `Downloading webpb-protoc-ts ${version} for ${webpbReleasePlatform()}...`,
    );
    await downloadReleaseBinary(version, cachedPath);
    return cachedPath;
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    console.warn(`Release download failed: ${message}`);
  }

  const rebuilt = resolvePluginAfterFailedDownload(root, exit);
  if (rebuilt) {
    return rebuilt;
  }

  console.error(
    [
      "webpb-protoc-ts not found.",
      "Install options:",
      "  - set WEBPB_PROTOC_TS to the plugin binary path",
      "  - download from https://github.com/jinganix/webpb/releases",
      "  - set WEBPB_VERSION and retry (auto-download)",
      "  - build from source: cd plugin && make build",
    ].join("\n"),
  );
  return exit(1);
}
