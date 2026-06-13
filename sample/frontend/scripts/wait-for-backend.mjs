import net from "node:net";

const host = "127.0.0.1";
const port = 8181;
const maxAttempts = 120;

function isBackendUp() {
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

for (let attempt = 0; attempt < maxAttempts; attempt++) {
  if (await isBackendUp()) {
    console.log(`Backend is ready on http://${host}:${port}`);
    process.exit(0);
  }
  if (attempt === 0) {
    console.log(`Waiting for backend on http://${host}:${port}...`);
  }
  await new Promise((resolve) => setTimeout(resolve, 1000));
}

console.error(`Timed out waiting for backend on http://${host}:${port}`);
process.exit(1);
