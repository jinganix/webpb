import { expect, test } from "@playwright/test";

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
});
