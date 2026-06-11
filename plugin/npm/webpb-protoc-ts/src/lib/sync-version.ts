export interface PackageJson {
  version: string;
}

export function parseGradleVersion(content: string): string | null {
  const versionLine = content
    .split("\n")
    .find((line) => line.startsWith("version ="));
  if (!versionLine) {
    return null;
  }
  return versionLine
    .split("=")[1]
    .trim()
    .replace(/-SNAPSHOT$/, "");
}

export function applyPackageVersion(
  pkg: PackageJson,
  version: string,
): boolean {
  if (pkg.version === version) {
    return false;
  }
  pkg.version = version;
  return true;
}

export function formatPackageJson(pkg: PackageJson): string {
  return `${JSON.stringify(pkg, null, 2)}\n`;
}
