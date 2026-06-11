import { dirname } from "node:path";
import { fileURLToPath } from "node:url";

export function packageRootFromModuleUrl(
  moduleUrl: string,
  segmentsUpFromFile: number,
): string {
  let dir = dirname(fileURLToPath(moduleUrl));
  for (let index = 0; index < segmentsUpFromFile; index++) {
    dir = dirname(dir);
  }
  return dir;
}

export function protocPath(
  filePath: string,
  platform: NodeJS.Platform = process.platform,
): string {
  if (platform === "win32") {
    return filePath.replace(/\\/g, "/");
  }
  return filePath;
}
