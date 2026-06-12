import { existsSync, readFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";
import { executableExtension } from "./platform.js";

export function packageRoot(moduleUrl: string | URL): string {
  let dir = dirname(fileURLToPath(moduleUrl));
  while (dir !== dirname(dir)) {
    if (existsSync(join(dir, "package.json"))) {
      return dir;
    }
    dir = dirname(dir);
  }
  throw new Error("webpb package root not found");
}

export function findMonorepoRoot(root: string): string | undefined {
  let dir = root;
  while (dir !== dirname(dir)) {
    if (existsSync(join(dir, "plugin", "go.mod"))) {
      return dir;
    }
    dir = dirname(dir);
  }
  return undefined;
}

export function bundledProtoInclude(root: string): string {
  const packaged = join(root, "proto");
  if (existsSync(packaged)) {
    return packaged;
  }
  const monorepoRoot = findMonorepoRoot(root);
  if (monorepoRoot) {
    const monorepo = join(monorepoRoot, "lib/proto/src/main/resources");
    if (existsSync(monorepo)) {
      return monorepo;
    }
  }
  throw new Error(
    "webpb proto includes not found; reinstall the webpb npm package",
  );
}

export function monorepoPluginPath(root: string): string | undefined {
  const monorepoRoot = findMonorepoRoot(root);
  if (!monorepoRoot) {
    return undefined;
  }
  const ext = executableExtension();
  return join(monorepoRoot, "plugin/bin", `webpb-protoc-ts${ext}`);
}

export function readPackageVersion(root: string): string {
  const pkg = JSON.parse(readFileSync(join(root, "package.json"), "utf8")) as {
    version: string;
  };
  return pkg.version;
}
