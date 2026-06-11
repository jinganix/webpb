import { readFileSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const packageDir = dirname(dirname(fileURLToPath(import.meta.url)));
const repoRoot = join(packageDir, '..', '..', '..');
const gradleProps = join(repoRoot, 'gradle.properties');
const packageJsonPath = join(packageDir, 'package.json');

const versionLine = readFileSync(gradleProps, 'utf8')
  .split('\n')
  .find((line) => line.startsWith('version ='));
if (!versionLine) {
  console.error('sync-version: version not found in gradle.properties');
  process.exit(1);
}

const version = versionLine.split('=')[1].trim().replace(/-SNAPSHOT$/, '');
const pkg = JSON.parse(readFileSync(packageJsonPath, 'utf8'));
if (pkg.version === version) {
  console.log(`sync-version: ${version} (unchanged)`);
  process.exit(0);
}

pkg.version = version;
writeFileSync(packageJsonPath, `${JSON.stringify(pkg, null, 2)}\n`);
console.log(`sync-version: ${version}`);
