import { spawn } from "node:child_process";
import net from "node:net";
import path from "node:path";
import { fileURLToPath } from "node:url";

const host = "127.0.0.1";
const backendPort = 8181;
const frontendPort = 4200;
const scriptDir = path.dirname(fileURLToPath(import.meta.url));
const frontendDir = path.resolve(scriptDir, "..");
const repoRoot = path.resolve(frontendDir, "../..");
const gradle = path.join(
  repoRoot,
  process.platform === "win32" ? "gradlew.bat" : "gradlew",
);

function isPortOpen(port) {
  return new Promise((resolve) => {
    const socket = net.connect(port, host);
    socket.setTimeout(500);
    socket.on("connect", () => {
      socket.end();
      resolve(true);
    });
    socket.on("error", () => resolve(false));
    socket.on("timeout", () => {
      socket.destroy();
      resolve(false);
    });
  });
}

async function waitForPort(port, label, maxAttempts = 180) {
  for (let attempt = 0; attempt < maxAttempts; attempt++) {
    if (await isPortOpen(port)) {
      console.log(`${label} is ready on http://${host}:${port}`);
      return;
    }
    if (attempt === 0) {
      console.log(`Waiting for ${label} on http://${host}:${port}...`);
    }
    await new Promise((resolve) => setTimeout(resolve, 1000));
  }
  throw new Error(`Timed out waiting for ${label} on http://${host}:${port}`);
}

async function ensureBackend() {
  if (await isPortOpen(backendPort)) {
    console.log(`Backend already listening on http://${host}:${backendPort}`);
    return;
  }

  const child = spawn(gradle, ["sample:backend:bootRun", "--no-daemon"], {
    cwd: repoRoot,
    stdio: "inherit",
  });
  child.on("exit", (code) => {
    if (code !== 0 && code !== null) {
      console.error(`Backend exited with code ${code}`);
      process.exit(code);
    }
  });

  await waitForPort(backendPort, "backend");
}

async function startFrontend() {
  const proto = spawn("npm", ["run", "proto"], {
    cwd: frontendDir,
    stdio: "inherit",
  });
  await new Promise((resolve, reject) => {
    proto.on("exit", (code) => {
      if (code === 0) {
        resolve();
        return;
      }
      reject(new Error(`proto generation failed with code ${code ?? 1}`));
    });
  });

  const vite = spawn(
    "npx",
    ["vite", "--host", host, "--port", String(frontendPort)],
    {
      cwd: frontendDir,
      stdio: "inherit",
    },
  );
  vite.on("exit", (code) => process.exit(code ?? 0));
  await waitForPort(frontendPort, "frontend", 60);
}

await ensureBackend();
await startFrontend();
