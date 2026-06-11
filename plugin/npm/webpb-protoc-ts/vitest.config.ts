import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    coverage: {
      exclude: ["src/bin/**", "src/scripts/**"],
      include: ["src/lib/**/*.ts"],
      reporter: ["text", "html", "lcov"],
    },
    watch: false,
  },
});
