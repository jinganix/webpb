import { spawnSync } from 'node:child_process';
import { existsSync, mkdirSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const scriptDir = dirname(fileURLToPath(import.meta.url));
const frontendDir = join(scriptDir, '..');
const repoRoot = join(frontendDir, '../..');
const pluginDir = join(repoRoot, 'plugin');
const ext = process.platform === 'win32' ? '.exe' : '';
const pluginBin = join(pluginDir, 'bin', `webpb-protoc-ts${ext}`);
const outDir = join(frontendDir, 'generated', 'proto');
const includeDirs = [
  join(repoRoot, 'lib/proto/src/main/resources'),
  join(repoRoot, 'sample/proto/src/main/resources'),
];
const protoFiles = ['Common.proto', 'Store.proto'].map((name) =>
  join(repoRoot, 'sample/proto/src/main/resources', name),
);

function ensurePlugin() {
  if (existsSync(pluginBin)) {
    return;
  }
  console.log('Building webpb-protoc-ts...');
  const result = spawnSync(
    'go',
    ['build', '-o', join('bin', `webpb-protoc-ts${ext}`), './cmd/webpb-ts'],
    { cwd: pluginDir, stdio: 'inherit' },
  );
  if (result.status !== 0) {
    process.exit(result.status ?? 1);
  }
}

function runProtoc() {
  mkdirSync(outDir, { recursive: true });
  const protoc = process.env.PROTOC ?? 'protoc';
  const args = [
    ...includeDirs.flatMap((dir) => ['-I', dir]),
    `--plugin=protoc-gen-ts=${pluginBin}`,
    `--ts_out=${outDir}`,
    ...protoFiles,
  ];
  const result = spawnSync(protoc, args, { stdio: 'inherit' });
  if (result.error) {
    console.error(
      `Failed to run ${protoc}. Install protoc or set PROTOC to its path.`,
    );
    process.exit(1);
  }
  process.exit(result.status ?? 1);
}

ensurePlugin();
runProtoc();
