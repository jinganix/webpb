import { spawnSync } from "node:child_process";
import { mkdirSync } from "node:fs";
import { join } from "node:path";

import { packageRootFromModuleUrl } from "../lib/paths.js";

const packageDir = packageRootFromModuleUrl(import.meta.url, 2);
const distDir = join(packageDir, "dist");

mkdirSync(distDir, { recursive: true });

const result = spawnSync("npm", ["pack", "--pack-destination", distDir], {
  cwd: packageDir,
  stdio: "inherit",
});

process.exit(result.status ?? 1);
