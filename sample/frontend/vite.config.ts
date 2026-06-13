import path from "node:path";
import { fileURLToPath } from "node:url";

import tailwindcss from "@tailwindcss/vite";
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

const rootDir = path.dirname(fileURLToPath(import.meta.url));
const backendTarget = "http://127.0.0.1:8181";

const proxy = {
  "/api": {
    changeOrigin: true,
    target: backendTarget,
  },
  "/options": {
    changeOrigin: true,
    target: backendTarget,
  },
};

export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      "@": path.resolve(rootDir, "src"),
      "@proto": path.resolve(rootDir, "generated/proto"),
    },
  },
  preview: {
    port: 4200,
    proxy,
  },
  server: {
    port: 4200,
    proxy,
  },
});
