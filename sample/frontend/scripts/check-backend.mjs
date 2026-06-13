import net from "node:net";

const host = "127.0.0.1";
const port = 8181;

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

if (!(await isBackendUp())) {
  console.warn("");
  console.warn(`Backend is not running on http://${host}:${port}.`);
  console.warn("API requests will fail with ERR_NETWORK until it starts.");
  console.warn("");
  console.warn("  ./gradlew sample:backend:bootRun");
  console.warn("  npm run start:all    # start backend + frontend together");
  console.warn("");
}
