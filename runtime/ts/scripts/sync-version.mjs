import { readFileSync, writeFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const packageDir = dirname(dirname(fileURLToPath(import.meta.url)));
const repoRoot = join(packageDir, "..", "..");
const gradleProps = join(repoRoot, "gradle.properties");
const packageJsonPath = join(packageDir, "package.json");
const versionPropsPath = join(packageDir, "webpb-version.properties");

const gradleText = readFileSync(gradleProps, "utf8");
const versionLine = gradleText
  .split("\n")
  .find((line) => line.startsWith("version ="));
if (!versionLine) {
  console.error("sync-version: version not found in gradle.properties");
  process.exit(1);
}

const protobufLine = gradleText
  .split("\n")
  .find((line) => line.startsWith("versionProtobufJava ="));
if (!protobufLine) {
  console.error("sync-version: versionProtobufJava not found in gradle.properties");
  process.exit(1);
}

const version = versionLine.split("=")[1].trim().replace(/-SNAPSHOT$/, "");
const protobufVersion = protobufLine.split("=")[1].trim();

const pkg = JSON.parse(readFileSync(packageJsonPath, "utf8"));
let changed = false;
if (pkg.version !== version) {
  pkg.version = version;
  writeFileSync(packageJsonPath, `${JSON.stringify(pkg, null, 2)}\n`);
  changed = true;
}

const versionProps = `webpbVersion=${version}\nprotobufVersion=${protobufVersion}\n`;
const currentProps = readFileSync(versionPropsPath, "utf8");
if (currentProps !== versionProps) {
  writeFileSync(versionPropsPath, versionProps);
  changed = true;
}

if (changed) {
  console.log(`sync-version: webpb ${version}, protobuf ${protobufVersion}`);
} else {
  console.log(`sync-version: webpb ${version} (unchanged)`);
}
