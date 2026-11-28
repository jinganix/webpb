export function resolveApiBaseUrl(): string {
  const configured = import.meta.env.VITE_API_BASE_URL?.trim();
  if (configured) {
    return configured.replace(/\/$/, "");
  }
  if (import.meta.env.DEV) {
    if (typeof window !== "undefined") {
      return window.location.origin;
    }
    return "http://127.0.0.1:4200";
  }
  return "";
}
