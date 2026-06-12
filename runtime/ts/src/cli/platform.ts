export function webpbReleasePlatform(
  platform: NodeJS.Platform = process.platform,
  arch: NodeJS.Architecture = process.arch,
): string {
  if (platform === "darwin") {
    return arch === "arm64" ? "darwin-arm64" : "darwin-amd64";
  }
  if (platform === "linux") {
    return arch === "arm64" ? "linux-arm64" : "linux-amd64";
  }
  if (platform === "win32") {
    return arch === "ia32" ? "windows-386" : "windows-amd64";
  }
  throw new Error(`Unsupported platform: ${platform} ${arch}`);
}

export function protocMavenClassifier(
  platform: NodeJS.Platform = process.platform,
  arch: NodeJS.Architecture = process.arch,
): string {
  if (platform === "darwin") {
    return arch === "arm64" ? "osx-aarch_64" : "osx-x86_64";
  }
  if (platform === "linux") {
    if (arch === "arm64") {
      return "linux-aarch_64";
    }
    if (arch === "ppc64") {
      return "linux-ppcle_64";
    }
    if (arch === "s390x") {
      return "linux-s390_64";
    }
    if (arch === "ia32") {
      return "linux-x86_32";
    }
    return "linux-x86_64";
  }
  if (platform === "win32") {
    return arch === "ia32" ? "windows-x86_32" : "windows-x86_64";
  }
  throw new Error(`Unsupported platform: ${platform} ${arch}`);
}

export function executableExtension(
  platform: NodeJS.Platform = process.platform,
): string {
  return platform === "win32" ? ".exe" : "";
}
