import { existsSync } from "node:fs";
import { join } from "node:path";

import { normalizeNodeArch } from "./platforms.js";

export type PluginBinarySource = "monorepo" | "vendor";

export interface ResolvedPluginBinary {
  path: string;
  source: PluginBinarySource;
}

export function pluginBinaryExt(platform: NodeJS.Platform): string {
  if (platform === "win32") {
    return ".exe";
  }
  return "";
}

export function vendorBinaryPath(
  packageDir: string,
  platform: NodeJS.Platform,
  arch: string,
): string {
  const ext = pluginBinaryExt(platform);
  const nodeArch = normalizeNodeArch(arch);
  return join(
    packageDir,
    "vendor",
    `${platform}-${nodeArch}`,
    `webpb-protoc-ts${ext}`,
  );
}

export function monorepoBinaryPath(
  packageDir: string,
  platform: NodeJS.Platform,
): string {
  const ext = pluginBinaryExt(platform);
  return join(packageDir, "..", "..", "bin", `webpb-protoc-ts${ext}`);
}

export function resolvePluginBinary(
  packageDir: string,
  exists: (path: string) => boolean = existsSync,
  platform: NodeJS.Platform = process.platform,
  arch: string = process.arch,
): ResolvedPluginBinary | null {
  const vendor = vendorBinaryPath(packageDir, platform, arch);
  if (exists(vendor)) {
    return { path: vendor, source: "vendor" };
  }
  const monorepo = monorepoBinaryPath(packageDir, platform);
  if (exists(monorepo)) {
    return { path: monorepo, source: "monorepo" };
  }
  return null;
}

export function runPluginBinary(
  binaryPath: string,
  argv: string[],
  spawn: (
    command: string,
    args: string[],
    options: { stdio: "inherit" },
  ) => { status: number | null },
): number {
  const result = spawn(binaryPath, argv, { stdio: "inherit" });
  return result.status ?? 1;
}
