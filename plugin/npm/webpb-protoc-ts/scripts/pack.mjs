import { spawnSync } from 'node:child_process';
import { mkdirSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const packageDir = dirname(dirname(fileURLToPath(import.meta.url)));
const distDir = join(packageDir, 'dist');

mkdirSync(distDir, { recursive: true });

const result = spawnSync('npm', ['pack', '--pack-destination', distDir], {
  cwd: packageDir,
  stdio: 'inherit',
});

process.exit(result.status ?? 1);
