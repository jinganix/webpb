import { readFileSync, writeFileSync } from "node:fs";
import { join } from "node:path";

import { packageRootFromModuleUrl } from "../lib/paths.js";
import {
  applyPackageVersion,
  formatPackageJson,
  parseGradleVersion,
  type PackageJson,
} from "../lib/sync-version.js";

const packageDir = packageRootFromModuleUrl(import.meta.url, 2);
const repoRoot = join(packageDir, "..", "..", "..");
const gradleProps = join(repoRoot, "gradle.properties");
const packageJsonPath = join(packageDir, "package.json");

const version = parseGradleVersion(readFileSync(gradleProps, "utf8"));
if (!version) {
  console.error("sync-version: version not found in gradle.properties");
  process.exit(1);
}

const pkg = JSON.parse(readFileSync(packageJsonPath, "utf8")) as PackageJson;
if (!applyPackageVersion(pkg, version)) {
  console.log(`sync-version: ${version} (unchanged)`);
  process.exit(0);
}

writeFileSync(packageJsonPath, formatPackageJson(pkg));
console.log(`sync-version: ${version}`);
