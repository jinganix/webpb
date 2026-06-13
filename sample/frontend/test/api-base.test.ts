import { afterEach, describe, expect, it, vi } from "vitest";

import { resolveApiBaseUrl, resolveDirectBackendUrl } from "@/lib/api-base";

describe("api-base", () => {
  afterEach(() => {
    vi.unstubAllEnvs();
  });

  it("should return configured base url when VITE_API_BASE_URL is set", () => {
    // Given
    vi.stubEnv("VITE_API_BASE_URL", "http://127.0.0.1:8181/");

    // When / Then
    expect(resolveApiBaseUrl()).toBe("http://127.0.0.1:8181");
    expect(resolveDirectBackendUrl()).toBe("http://127.0.0.1:8181");
  });

  it("should return window origin in development when env is unset", () => {
    // Given
    vi.stubEnv("DEV", "true");
    vi.stubEnv("VITE_API_BASE_URL", "");

    // When / Then
    expect(resolveApiBaseUrl()).toBe(window.location.origin);
    expect(resolveDirectBackendUrl()).toBe("http://127.0.0.1:8181");
  });

  it("should return empty base url in production when env is unset", () => {
    // Given
    vi.stubEnv("DEV", "");
    vi.stubEnv("VITE_API_BASE_URL", "");

    // When / Then
    expect(resolveApiBaseUrl()).toBe("");
  });
});
