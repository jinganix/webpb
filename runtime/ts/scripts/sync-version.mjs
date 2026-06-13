import { spawnSync } from "node:child_process";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const repoRoot = join(dirname(dirname(fileURLToPath(import.meta.url))), "..", "..");
const result = spawnSync(
  process.execPath,
  [join(repoRoot, "scripts", "sync-versions.mjs"), ...process.argv.slice(2)],
  { stdio: "inherit" },
);

if (result.status !== 0) {
  process.exit(result.status ?? 1);
}
