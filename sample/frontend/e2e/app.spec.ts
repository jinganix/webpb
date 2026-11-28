import { expect, test, type Locator, type Page } from "@playwright/test";

async function sendExampleRequest(page: Page, name: RegExp): Promise<void> {
  await page.getByRole("button", { name }).click();
  await page.getByRole("button", { name: "Send request" }).click();
}

function responsePanel(page: Page): Locator {
  return page
    .getByRole("heading", { exact: true, name: "Response" })
    .locator("..");
}

test.describe("sample app", () => {
  test("should render option examples when page loads", async ({ page }) => {
    // Given / When
    await page.goto("/");

    // Then
    await expect(
      page.getByRole("heading", { name: "Proto options" }),
    ).toBeVisible();
    await expect(page.getByText("File options (f_opts)")).toBeVisible();
    await expect(page.getByText("HTTP route")).toBeVisible();
  });

  test("should render store examples when page loads", async ({ page }) => {
    // Given / When
    await page.goto("/");

    // Then
    await expect(
      page.getByRole("heading", { name: "Stores" }),
    ).toBeVisible();
    await expect(page.getByText("Store greeting")).toBeVisible();
    await expect(page.getByText("Store list")).toBeVisible();
    await expect(page.getByText("Store visit")).toBeVisible();
  });

  test("should show echo response when HTTP route sends request", async ({
    page,
  }) => {
    // Given
    await page.goto("/");
    await page.getByRole("button", { name: /HTTP route/i }).click();

    // When
    await page.getByRole("button", { name: "Send request" }).click();

    // Then
    await expect(page.getByText(/echo: webpb/)).toBeVisible();
  });

  test("should show validation error when code field is empty", async ({
    page,
  }) => {
    // Given
    await page.goto("/");
    await page.getByRole("button", { name: /Validation/i }).click();
    await page.getByLabel("code").clear();

    // When
    await page.getByRole("button", { name: "Send request" }).click();

    // Then
    await expect(page.getByText(/must not be blank/)).toBeVisible();
  });

  test("should show resolved paths when context path sends request", async ({
    page,
  }) => {
    // Given
    await page.goto("/");

    // When
    await sendExampleRequest(page, /Context path/i);

    // Then
    await expect(
      responsePanel(page).getByText(/\/api\/options\/context/),
    ).toBeVisible();
  });

  test("should show echoed tag when query parameter sends request", async ({
    page,
  }) => {
    // Given
    await page.goto("/");

    // When
    await sendExampleRequest(page, /Query parameter/i);

    // Then
    await expect(responsePanel(page).getByText(/"tag": "demo"/)).toBeVisible();
  });

  test("should show echoed id when int64 string sends request", async ({
    page,
  }) => {
    // Given
    await page.goto("/");

    // When
    await sendExampleRequest(page, /int64 as string/i);

    // Then
    await expect(
      responsePanel(page).getByText(/9007199254740991/),
    ).toBeVisible();
  });

  test("should show aliased payload when auto alias sends request", async ({
    page,
  }) => {
    // Given
    await page.goto("/");

    // When
    await sendExampleRequest(page, /Auto alias/i);

    // Then
    await expect(responsePanel(page).getByText(/hello/)).toBeVisible();
  });

  test("should show echoed title when field alias sends request", async ({
    page,
  }) => {
    // Given
    await page.goto("/");

    // When
    await sendExampleRequest(page, /Field alias/i);

    // Then
    await expect(responsePanel(page).getByText(/Webpb/)).toBeVisible();
  });

  test("should show active status when enum string sends request", async ({
    page,
  }) => {
    // Given
    await page.goto("/");

    // When
    await sendExampleRequest(page, /Enum string/i);

    // Then
    await expect(responsePanel(page).getByText(/active/)).toBeVisible();
  });

  test("should return greeting when store greeting sends request", async ({
    page,
  }) => {
    // Given
    await page.goto("/");

    // When
    await sendExampleRequest(page, /Store greeting/i);

    // Then
    await expect(
      responsePanel(page).getByText(/Welcome, webpb/),
    ).toBeVisible();
  });

  test("should list stores when store list sends request", async ({
    page,
  }) => {
    // Given
    await page.goto("/");

    // When
    await sendExampleRequest(page, /Store list/i);

    // Then
    await expect(responsePanel(page).getByText(/store-0/)).toBeVisible();
    await expect(responsePanel(page).getByText(/store-1/)).toBeVisible();
  });

  test("should return second page when store list requests page two", async ({
    page,
  }) => {
    // Given
    await page.goto("/");
    await page.getByRole("button", { name: /Store list/i }).click();
    await page.getByLabel("page").fill("2");

    // When
    await page.getByRole("button", { name: "Send request" }).click();

    // Then
    await expect(responsePanel(page).getByText(/"page": 2/)).toBeVisible();
    await expect(responsePanel(page).getByText(/store-2/)).toBeVisible();
  });

  test("should show validation error when store list size exceeds limit", async ({
    page,
  }) => {
    // Given
    await page.goto("/");
    await page.getByRole("button", { name: /Store list/i }).click();
    await page.getByLabel("size").fill("11");

    // When
    await page.getByRole("button", { name: "Send request" }).click();

    // Then
    await expect(
      page.getByText(/must be less than or equal to 10/),
    ).toBeVisible();
  });

  test("should return store details when store visit sends request", async ({
    page,
  }) => {
    // Given
    await page.goto("/");

    // When
    await sendExampleRequest(page, /Store visit/i);

    // Then
    await expect(responsePanel(page).getByText(/store-123/)).toBeVisible();
    await expect(
      responsePanel(page).getByText(/Welcome, webpb/),
    ).toBeVisible();
  });

  test("should show validation error when store visit customer is blank", async ({
    page,
  }) => {
    // Given
    await page.goto("/");
    await page.getByRole("button", { name: /Store visit/i }).click();
    await page.getByLabel("customer").clear();

    // When
    await page.getByRole("button", { name: "Send request" }).click();

    // Then
    await expect(page.getByText(/must not be blank/)).toBeVisible();
  });
});
