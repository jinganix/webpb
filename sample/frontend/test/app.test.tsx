import axios from "axios";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

import { App } from "@/App";
import { ExampleCard } from "@/components/example-card";
import {
  FILE_OPTION_EXAMPLE,
  OPTION_EXAMPLES,
} from "@/options/option-definitions";
import { HttpService } from "@/services/http.service";

vi.mock("axios");

describe("HttpService", () => {
  const httpService = new HttpService("http://127.0.0.1:4200");

  it("should return response log when request succeeds", async () => {
    // Given
    const request = OPTION_EXAMPLES[0].createRequest({ name: "demo" });
    vi.mocked(axios.request).mockResolvedValue({
      data: { echo: "echo: demo" },
    });

    // When
    const result = await httpService.request(
      request,
      OPTION_EXAMPLES[0].responseType,
    );

    // Then
    expect(result.log.url).toContain("/options/http-route");
    expect(result.data).toMatchObject({ echo: "echo: demo" });
  });

  it("should return raw data when response type is omitted", async () => {
    // Given
    const request = OPTION_EXAMPLES[0].createRequest({ name: "demo" });
    vi.mocked(axios.request).mockResolvedValue({
      data: { echo: "echo: demo" },
    });

    // When
    const result = await httpService.request(request);

    // Then
    expect(result.data).toEqual({ echo: "echo: demo" });
  });

  it("should attach error log when request fails with response", async () => {
    // Given
    const request = OPTION_EXAMPLES[0].createRequest({ name: "demo" });
    vi.mocked(axios.request).mockRejectedValue({
      response: {
        data: { errors: { code: "must not be blank" } },
        headers: {},
        status: 400,
      },
    });

    // When / Then
    await expect(httpService.request(request)).rejects.toMatchObject({
      log: {
        error: {
          data: { errors: { code: "must not be blank" } },
          status: 400,
        },
      },
    });
  });

  it("should attach raw error when request fails without response", async () => {
    // Given
    const request = OPTION_EXAMPLES[0].createRequest({ name: "demo" });
    const networkError = {
      code: "ERR_NETWORK",
      message: "Network Error",
    };
    vi.mocked(axios.request).mockRejectedValue(networkError);

    // When / Then
    await expect(httpService.request(request)).rejects.toMatchObject({
      log: {
        error: {
          hint: expect.stringContaining("bootRun"),
          message: expect.stringContaining("Network error"),
        },
      },
    });
  });

  it("should attach unknown error when request fails without axios code", async () => {
    // Given
    const request = OPTION_EXAMPLES[0].createRequest({ name: "demo" });
    const unknownError = new Error("boom");
    vi.mocked(axios.request).mockRejectedValue(unknownError);

    // When / Then
    await expect(httpService.request(request)).rejects.toMatchObject({
      log: {
        error: unknownError,
      },
    });
  });
});

describe("option definitions", () => {
  it("should apply defaults when createRequest values are empty", () => {
    // Given / When
    const enumRequest = OPTION_EXAMPLES[7].createRequest({});

    // Then
    expect(enumRequest).toMatchObject({ status: expect.anything() });
  });

  it("should map active status when enum example receives active", () => {
    // Given / When
    const enumRequest = OPTION_EXAMPLES[7].createRequest({ status: "active" });

    // Then
    expect(enumRequest).toMatchObject({ status: "active" });
  });

  it("should use explicit field values when createRequest receives them", () => {
    // Given / When / Then
    expect(OPTION_EXAMPLES[0].createRequest({ name: "custom" })).toMatchObject({
      name: "custom",
    });
    expect(OPTION_EXAMPLES[3].createRequest({ code: "x" })).toMatchObject({
      code: "x",
    });
    expect(OPTION_EXAMPLES[4].createRequest({ id: "42" })).toMatchObject({
      id: "42",
    });
    expect(OPTION_EXAMPLES[5].createRequest({ text: "alias" })).toMatchObject({
      text: "alias",
    });
    expect(OPTION_EXAMPLES[6].createRequest({ title: "Demo" })).toMatchObject({
      title: "Demo",
    });
  });

  it("should create requests for every example using defaults", () => {
    // Given / When / Then
    for (const example of [FILE_OPTION_EXAMPLE, ...OPTION_EXAMPLES]) {
      expect(example.createRequest({})).toBeDefined();
      for (const field of example.fields) {
        expect(
          example.createRequest({ [field.key]: field.defaultValue }),
        ).toBeDefined();
      }
    }
  });
});

describe("ExampleCard", () => {
  async function expandExampleCard(
    user: ReturnType<typeof userEvent.setup>,
    title: string | RegExp,
  ) {
    await user.click(screen.getByRole("button", { name: title }));
  }

  it("should render proto snippet when example card expands", async () => {
    // Given
    const user = userEvent.setup();
    render(
      <ExampleCard
        example={OPTION_EXAMPLES[0]}
        httpService={new HttpService("")}
      />,
    );

    // When
    await expandExampleCard(user, /HTTP route/i);

    // Then
    expect(screen.getByText("HTTP route")).toBeInTheDocument();
    expect(screen.getByText(/HttpRouteRequest/)).toBeInTheDocument();
  });

  it("should show response data when send request succeeds", async () => {
    // Given
    const user = userEvent.setup();
    vi.mocked(axios.request).mockResolvedValue({
      data: { echo: "echo: webpb" },
    });
    render(
      <ExampleCard
        example={OPTION_EXAMPLES[0]}
        httpService={new HttpService("")}
      />,
    );
    await expandExampleCard(user, /HTTP route/i);

    // When
    await user.click(screen.getByRole("button", { name: "Send request" }));

    // Then
    expect(screen.getByText(/echo: webpb/)).toBeInTheDocument();
  });

  it("should show error data when send request fails", async () => {
    // Given
    const user = userEvent.setup();
    vi.mocked(axios.request).mockRejectedValue({
      response: {
        data: { errors: { code: "must not be blank" } },
        headers: {},
        status: 400,
      },
    });
    render(
      <ExampleCard
        example={OPTION_EXAMPLES[3]}
        httpService={new HttpService("")}
      />,
    );
    await expandExampleCard(user, /Validation/i);

    // When
    await user.clear(screen.getByLabelText("code"));
    await user.click(screen.getByRole("button", { name: "Send request" }));

    // Then
    expect(screen.getByText(/must not be blank/)).toBeInTheDocument();
  });

  it("should update request preview when input changes", async () => {
    // Given
    const user = userEvent.setup();
    render(
      <ExampleCard
        example={OPTION_EXAMPLES[0]}
        httpService={new HttpService("")}
      />,
    );
    await expandExampleCard(user, /HTTP route/i);

    // When
    await user.clear(screen.getByLabelText("name"));
    await user.type(screen.getByLabelText("name"), "vite");

    // Then
    expect(screen.getByText(/vite/)).toBeInTheDocument();
  });

  it("should show placeholder when example card expands before request", async () => {
    // Given
    const user = userEvent.setup();
    render(
      <ExampleCard
        example={OPTION_EXAMPLES[0]}
        httpService={new HttpService("")}
      />,
    );

    // When
    await expandExampleCard(user, /HTTP route/i);

    // Then
    expect(screen.getByText(/No response yet/)).toBeInTheDocument();
  });
});

describe("App", () => {
  beforeEach(() => {
    vi.mocked(axios.request).mockResolvedValue({ data: {} });
  });

  it("should render option examples when app mounts", () => {
    // Given / When
    render(<App />);

    // Then
    expect(screen.getByText("Proto options")).toBeInTheDocument();
    expect(screen.getByText("File options (f_opts)")).toBeInTheDocument();
  });
});
