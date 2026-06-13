import { execSync } from "node:child_process";

const args =
  process.platform === "linux"
    ? "chromium --with-deps"
    : "chromium";

execSync(`npx playwright install ${args}`, { stdio: "inherit" });
