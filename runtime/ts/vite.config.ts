/// <reference types="vitest" />
import path from "path";
import { defineConfig, LibraryFormats } from "vite";

const FORMATS: Partial<Record<LibraryFormats, string>> = {
  cjs: `index.cjs`,
  es: `index.mjs`,
};

export default defineConfig({
  base: "./",
  build: {
    lib: {
      entry: path.resolve(__dirname, "src/index.ts"),
      fileName: (format) => FORMATS[format],
      formats: Object.keys(FORMATS) as LibraryFormats[],
      name: "webpb",
    },
    outDir: "./build/dist",
  },
  test: {
    coverage: {
      include: ["src/**/*.ts"],
      reporter: ["text", "html", "lcov"],
      thresholds: {
        perFile: true,
        branches: 90,
        functions: 90,
        lines: 90,
        statements: 90,
      },
    },
    watch: false,
  },
});
