import { spawnSync } from "node:child_process";
import { mkdirSync } from "node:fs";
import { join } from "node:path";

import { packageRootFromModuleUrl } from "../lib/paths.js";
import { hostPlatform, PLATFORMS } from "../lib/platforms.js";
import { pluginBinaryExt } from "../lib/resolve-binary.js";

const packageDir = packageRootFromModuleUrl(import.meta.url, 2);
const pluginDir = join(packageDir, "..", "..");
const vendorDir = join(packageDir, "vendor");

function buildPlatform(platform: (typeof PLATFORMS)[number]): void {
  const ext = pluginBinaryExt(platform.nodeOs);
  const outDir = join(vendorDir, `${platform.nodeOs}-${platform.nodeArch}`);
  const outFile = join(outDir, `webpb-protoc-ts${ext}`);
  mkdirSync(outDir, { recursive: true });

  console.log(`build-vendor: ${platform.nodeOs}-${platform.nodeArch}`);
  const result = spawnSync("go", ["build", "-o", outFile, "./cmd/webpb-ts"], {
    cwd: pluginDir,
    env: {
      ...process.env,
      GOARCH: platform.goArch,
      GOOS: platform.goOs,
    },
    stdio: "inherit",
  });
  if (result.status !== 0) {
    process.exit(result.status ?? 1);
  }
}

const platforms = process.argv.includes("--all") ? PLATFORMS : [hostPlatform()];

for (const platform of platforms) {
  buildPlatform(platform);
}
