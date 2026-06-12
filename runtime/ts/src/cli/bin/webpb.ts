#!/usr/bin/env node

import { realpathSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { runGenerate } from "../generate.js";

export const HELP = `webpb — TypeScript runtime and protoc tooling for webpb

Usage:
  webpb generate [options] -- <files.proto...>

Run "webpb generate --help" for generation options.
`;

export async function runCli(argv: string[]): Promise<number> {
  const [command, ...rest] = argv;

  if (!command || command === "-h" || command === "--help") {
    process.stdout.write(HELP);
    return 0;
  }

  if (command === "generate") {
    return runGenerate(rest);
  }

  console.error(`Unknown command: ${command}`);
  process.stdout.write(HELP);
  return 1;
}

function normalizeComparablePath(path: string): string {
  if (process.platform !== "win32") {
    return path;
  }
  return path.replace(/^\\\\\?\\/, "").toLowerCase();
}

function resolveArgvEntryPath(argv: string[]): string {
  const entry = argv[1];
  if (!entry) {
    return "";
  }
  try {
    return realpathSync(entry);
  } catch {
    return entry;
  }
}

function resolveModulePath(moduleUrl: string | URL): string {
  try {
    return realpathSync(fileURLToPath(moduleUrl));
  } catch {
    return fileURLToPath(moduleUrl);
  }
}

export function isMainModule(
  moduleUrl: string | URL,
  argv: string[] = process.argv,
): boolean {
  const modulePath = normalizeComparablePath(resolveModulePath(moduleUrl));
  const entryPath = normalizeComparablePath(resolveArgvEntryPath(argv));
  return entryPath === modulePath;
}

export async function runMainEntrypoint(
  argv: string[] = process.argv,
  moduleUrl: string | URL = import.meta.url,
): Promise<number | undefined> {
  if (!isMainModule(moduleUrl, argv)) {
    return undefined;
  }
  return runCli(argv.slice(2));
}

export async function bootstrapMain(
  argv: string[] = process.argv,
  moduleUrl: string | URL = import.meta.url,
): Promise<void> {
  if (!isMainModule(moduleUrl, argv)) {
    return;
  }
  process.exit((await runMainEntrypoint(argv, moduleUrl)) ?? 0);
}

if (process.env.VITEST !== "true") {
  await bootstrapMain();
}
