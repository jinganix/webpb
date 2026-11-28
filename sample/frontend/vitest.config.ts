import path from "node:path";
import { fileURLToPath } from "node:url";

import react from "@vitejs/plugin-react";
import { defineConfig } from "vitest/config";

const rootDir = path.dirname(fileURLToPath(import.meta.url));

// @testing-library/react needs the React dev build (React.act). An outer
// NODE_ENV=production would resolve react-dom production (no act) and fail
// test/app.test.tsx, so pin vitest to a dev-like env.
process.env.NODE_ENV ??= "test";
if (process.env.NODE_ENV === "production") process.env.NODE_ENV = "test";

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(rootDir, "src"),
      "@proto": path.resolve(rootDir, "generated/proto"),
    },
  },
  test: {
    coverage: {
      exclude: ["e2e/**", "generated/**", "src/main.tsx"],
      include: ["src/**/*.{ts,tsx}"],
      provider: "v8",
      reporter: ["text", "lcov"],
      thresholds: {
        branches: 85,
        functions: 90,
        lines: 90,
        statements: 90,
      },
    },
    environment: "jsdom",
    exclude: ["e2e/**", "node_modules/**"],
    globals: true,
    setupFiles: ["./test/setup.ts"],
  },
});
