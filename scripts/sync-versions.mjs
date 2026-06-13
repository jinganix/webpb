import { readFileSync, writeFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const repoRoot = join(dirname(fileURLToPath(import.meta.url)), "..");
const gradlePropsPath = join(repoRoot, "gradle.properties");
const runtimeTsDir = join(repoRoot, "runtime", "ts");
const packageJsonPath = join(runtimeTsDir, "package.json");
const versionPropsPath = join(runtimeTsDir, "webpb-version.properties");
const packageLockPath = join(runtimeTsDir, "package-lock.json");
const pomPath = join(repoRoot, "sample", "backend", "pom.xml");
const readmePaths = ["README.md", "README.zh.md"].map((name) =>
  join(repoRoot, name),
);

function readGradleVersions() {
  const gradleText = readFileSync(gradlePropsPath, "utf8");
  const readProperty = (key) => {
    const line = gradleText
      .split("\n")
      .find((entry) => entry.startsWith(`${key} =`));
    if (!line) {
      throw new Error(`sync-versions: ${key} not found in gradle.properties`);
    }
    return line.split("=")[1].trim();
  };

  return {
    webpb: readProperty("version").replace(/-SNAPSHOT$/, ""),
    protobuf: readProperty("versionProtobufJava"),
  };
}

function parseArgs() {
  const args = process.argv.slice(2);
  let consumerVersion = null;
  let syncConsumerFiles = false;
  for (let index = 0; index < args.length; index++) {
    if (args[index] === "--consumer-version" && args[index + 1]) {
      consumerVersion = args[++index];
      syncConsumerFiles = true;
    } else if (args[index] === "--all") {
      syncConsumerFiles = true;
    }
  }
  return { consumerVersion, syncConsumerFiles };
}

function syncRuntimeTs(webpbVersion, protobufVersion) {
  const pkg = JSON.parse(readFileSync(packageJsonPath, "utf8"));
  let changed = false;

  if (pkg.version !== webpbVersion) {
    pkg.version = webpbVersion;
    writeFileSync(packageJsonPath, `${JSON.stringify(pkg, null, 2)}\n`);
    changed = true;
  }

  const versionProps = `webpbVersion=${webpbVersion}\nprotobufVersion=${protobufVersion}\n`;
  const currentProps = readFileSync(versionPropsPath, "utf8");
  if (currentProps !== versionProps) {
    writeFileSync(versionPropsPath, versionProps);
    changed = true;
  }

  const lock = JSON.parse(readFileSync(packageLockPath, "utf8"));
  if (lock.version !== webpbVersion || lock.packages?.[""]?.version !== webpbVersion) {
    lock.version = webpbVersion;
    if (lock.packages?.[""]) {
      lock.packages[""].version = webpbVersion;
    }
    writeFileSync(packageLockPath, `${JSON.stringify(lock, null, 2)}\n`);
    changed = true;
  }

  return changed;
}

function syncPomXml(webpbVersion, protobufVersion, { webpb = true, protobuf = true } = {}) {
  let content = readFileSync(pomPath, "utf8");
  const original = content;

  if (webpb) {
    content = content.replace(
      /<webpb\.version>[^<]+<\/webpb\.version>/,
      `<webpb.version>${webpbVersion}</webpb.version>`,
    );
  }
  if (protobuf) {
    content = content.replace(
      /<protobuf\.version>[^<]+<\/protobuf\.version>/,
      `<protobuf.version>${protobufVersion}</protobuf.version>`,
    );
  }

  if (content !== original) {
    writeFileSync(pomPath, content);
    return true;
  }
  return false;
}

function readPomPluginVersions() {
  const pomXml = readFileSync(pomPath, "utf8");
  const readTag = (tag) => {
    const match = pomXml.match(new RegExp(`<${tag}>([^<]+)</${tag}>`));
    if (!match) {
      throw new Error(`sync-versions: <${tag}> not found in sample/backend/pom.xml`);
    }
    return match[1];
  };

  return {
    osMavenPlugin: readTag("os.maven.plugin.version"),
    protobufMavenPlugin: readTag("protobuf.maven.plugin.version"),
  };
}

function syncReadme(webpbVersion, protobufVersion) {
  const { osMavenPlugin, protobufMavenPlugin } = readPomPluginVersions();
  let anyChanged = false;

  for (const readmePath of readmePaths) {
    let content = readFileSync(readmePath, "utf8");
    const original = content;

    content = content.replace(
      /id\("io\.github\.jinganix\.webpb\.(?:java|ts)"\) version "[0-9]+\.[0-9]+\.[0-9]+"/g,
      (match) => match.replace(/[0-9]+\.[0-9]+\.[0-9]+/, webpbVersion),
    );
    content = content.replace(
      /io\.github\.jinganix\.webpb:[a-z0-9-]+:[0-9]+\.[0-9]+\.[0-9]+(?::all@jar)?/g,
      (match) => match.replace(/[0-9]+\.[0-9]+\.[0-9]+/, webpbVersion),
    );
    content = content.replace(
      /webpbVersion = "[0-9]+\.[0-9]+\.[0-9]+"/g,
      `webpbVersion = "${webpbVersion}"`,
    );
    content = content.replace(
      /<webpb\.version>[0-9]+\.[0-9]+\.[0-9]+<\/webpb\.version>/g,
      `<webpb.version>${webpbVersion}</webpb.version>`,
    );
    content = content.replace(
      /<protobuf\.version>[0-9]+\.[0-9]+\.[0-9]+<\/protobuf\.version>/g,
      `<protobuf.version>${protobufVersion}</protobuf.version>`,
    );
    content = content.replace(
      /artifact = "com\.google\.protobuf:protoc:[0-9]+\.[0-9]+\.[0-9]+"/g,
      `artifact = "com.google.protobuf:protoc:${protobufVersion}"`,
    );
    content = content.replace(
      /protobufVersion = "[0-9]+\.[0-9]+\.[0-9]+"/g,
      `protobufVersion = "${protobufVersion}"`,
    );
    content = content.replace(
      /(<groupId>kr\.motd\.maven<\/groupId>\s*\n\s*<artifactId>os-maven-plugin<\/artifactId>\s*\n\s*<version>)[^<]+(<\/version>)/g,
      `$1${osMavenPlugin}$2`,
    );
    content = content.replace(
      /(<groupId>org\.xolstice\.maven\.plugins<\/groupId>\s*\n\s*<artifactId>protobuf-maven-plugin<\/artifactId>\s*\n\s*<version>)[^<]+(<\/version>)/g,
      `$1${protobufMavenPlugin}$2`,
    );

    if (content !== original) {
      writeFileSync(readmePath, content);
      anyChanged = true;
    }
  }

  return anyChanged;
}

const gradleVersions = readGradleVersions();
const { consumerVersion, syncConsumerFiles } = parseArgs();
const devWebpbVersion = gradleVersions.webpb;
const consumerWebpbVersion = consumerVersion ?? devWebpbVersion;
const protobufVersion = gradleVersions.protobuf;

const runtimeChanged = syncRuntimeTs(devWebpbVersion, protobufVersion);
const pomChanged = syncConsumerFiles
  ? syncPomXml(consumerWebpbVersion, protobufVersion)
  : syncPomXml(consumerWebpbVersion, protobufVersion, { webpb: false });
const readmeChanged = syncConsumerFiles
  ? syncReadme(consumerWebpbVersion, protobufVersion)
  : false;

if (runtimeChanged || pomChanged || readmeChanged) {
  if (syncConsumerFiles) {
    console.log(
      `sync-versions: dev ${devWebpbVersion}, consumer ${consumerWebpbVersion}, protobuf ${protobufVersion}`,
    );
  } else {
    console.log(
      `sync-versions: dev ${devWebpbVersion}, protobuf ${protobufVersion}`,
    );
  }
} else {
  console.log(`sync-versions: dev ${devWebpbVersion} (unchanged)`);
}
