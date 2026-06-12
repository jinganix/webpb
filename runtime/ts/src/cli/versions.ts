import { readFileSync } from "node:fs";
import { join } from "node:path";

export interface BundledVersions {
  webpbVersion: string;
  protobufVersion: string;
}

export function readBundledVersions(root: string): BundledVersions {
  const path = join(root, "webpb-version.properties");
  const text = readFileSync(path, "utf8");
  const props = Object.fromEntries(
    text
      .split("\n")
      .map((line) => line.trim())
      .filter((line) => line && !line.startsWith("#"))
      .map((line) => {
        const separator = line.indexOf("=");
        return [line.slice(0, separator), line.slice(separator + 1)];
      }),
  );
  if (!props.webpbVersion || !props.protobufVersion) {
    throw new Error(`Invalid webpb-version.properties at ${path}`);
  }
  return {
    protobufVersion: props.protobufVersion,
    webpbVersion: props.webpbVersion,
  };
}
