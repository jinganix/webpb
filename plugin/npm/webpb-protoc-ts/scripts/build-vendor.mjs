import { spawnSync } from 'node:child_process';
import { mkdirSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const PLATFORMS = [
  { nodeOs: 'darwin', nodeArch: 'arm64', goOs: 'darwin', goArch: 'arm64' },
  { nodeOs: 'darwin', nodeArch: 'x64', goOs: 'darwin', goArch: 'amd64' },
  { nodeOs: 'linux', nodeArch: 'x64', goOs: 'linux', goArch: 'amd64' },
  { nodeOs: 'win32', nodeArch: 'x64', goOs: 'windows', goArch: 'amd64' },
];

const packageDir = dirname(dirname(fileURLToPath(import.meta.url)));
const pluginDir = join(packageDir, '..', '..');
const vendorDir = join(packageDir, 'vendor');

function hostPlatform() {
  const arch = process.arch === 'amd64' ? 'x64' : process.arch;
  const platform =
    PLATFORMS.find(
      (item) => item.nodeOs === process.platform && item.nodeArch === arch,
    ) ?? PLATFORMS[2];
  return platform;
}

function buildPlatform(platform) {
  const ext = platform.goOs === 'windows' ? '.exe' : '';
  const outDir = join(vendorDir, `${platform.nodeOs}-${platform.nodeArch}`);
  const outFile = join(outDir, `webpb-protoc-ts${ext}`);
  mkdirSync(outDir, { recursive: true });

  console.log(`build-vendor: ${platform.nodeOs}-${platform.nodeArch}`);
  const result = spawnSync(
    'go',
    ['build', '-o', outFile, './cmd/webpb-ts'],
    {
      cwd: pluginDir,
      env: {
        ...process.env,
        GOOS: platform.goOs,
        GOARCH: platform.goArch,
      },
      stdio: 'inherit',
    },
  );
  if (result.status !== 0) {
    process.exit(result.status ?? 1);
  }
}

const platforms = process.argv.includes('--all')
  ? PLATFORMS
  : [hostPlatform()];

for (const platform of platforms) {
  buildPlatform(platform);
}
