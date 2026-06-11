export interface PlatformTarget {
  goArch: string;
  goOs: string;
  nodeArch: string;
  nodeOs: NodeJS.Platform;
}

export const PLATFORMS: PlatformTarget[] = [
  { goArch: "arm64", goOs: "darwin", nodeArch: "arm64", nodeOs: "darwin" },
  { goArch: "amd64", goOs: "darwin", nodeArch: "x64", nodeOs: "darwin" },
  { goArch: "amd64", goOs: "linux", nodeArch: "x64", nodeOs: "linux" },
  { goArch: "amd64", goOs: "windows", nodeArch: "x64", nodeOs: "win32" },
];

export function normalizeNodeArch(arch: string): string {
  if (arch === "amd64") {
    return "x64";
  }
  return arch;
}

export function hostPlatform(
  platform: NodeJS.Platform = process.platform,
  arch: string = process.arch,
): PlatformTarget {
  const nodeArch = normalizeNodeArch(arch);
  const match = PLATFORMS.find(
    (item) => item.nodeOs === platform && item.nodeArch === nodeArch,
  );
  if (match) {
    return match;
  }
  return PLATFORMS[2];
}
