import path from "node:path";
import { fileURLToPath } from "node:url";

import { defineConfig } from "@playwright/test";

const frontendDir = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  forbidOnly: Boolean(process.env.CI),
  fullyParallel: false,
  retries: process.env.CI ? 1 : 0,
  testDir: "./e2e",
  use: {
    baseURL: "http://127.0.0.1:4200",
    trace: "on-first-retry",
  },
  webServer: {
    command: "node scripts/start-e2e-servers.mjs",
    cwd: frontendDir,
    reuseExistingServer: !process.env.CI,
    timeout: 240_000,
    url: "http://127.0.0.1:4200",
  },
  workers: 1,
});
