import { chmodSync } from "node:fs";
import { cpSync, mkdirSync, readFileSync, writeFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const packageDir = dirname(dirname(fileURLToPath(import.meta.url)));
const repoRoot = join(packageDir, "..", "..");
const buildDir = join(packageDir, "build");

mkdirSync(buildDir, { recursive: true });

cpSync(
  join(repoRoot, "lib/proto/src/main/resources"),
  join(buildDir, "proto"),
  { recursive: true },
);
cpSync(
  join(packageDir, "webpb-version.properties"),
  join(buildDir, "webpb-version.properties"),
);

const cliBin = join(buildDir, "cli/bin/webpb.js");
if (process.platform !== "win32") {
  chmodSync(cliBin, 0o755);
}

const pkg = JSON.parse(readFileSync(join(packageDir, "package.json"), "utf8"));
const publishPkg = {
  author: pkg.author,
  bin: pkg.bin,
  description: pkg.description,
  exports: pkg.exports,
  files: pkg.files,
  keywords: pkg.keywords,
  license: pkg.license,
  main: pkg.main,
  module: pkg.module,
  name: pkg.name,
  repository: pkg.repository,
  sideEffects: pkg.sideEffects,
  type: pkg.type,
  types: pkg.types,
  version: pkg.version,
};

writeFileSync(
  join(buildDir, "package.json"),
  `${JSON.stringify(publishPkg, null, 2)}\n`,
);

console.log("prepare-build: copied proto, webpb-version.properties, and package.json");
