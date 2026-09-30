import { expect, test } from "@playwright/test";
import { freshName, freshPhone, signIn } from "./helpers";

/**
 * The SoftZenith console in the browser: a platform admin onboards a business through the form, and the business works
 * straight away: its enquiry form takes enquiries and its invited admin signs in to their own CRM.
 * Uses a throwaway web address so the real one (westernworld) stays free for the real onboarding.
 */
test("a platform admin onboards a business that is immediately usable", async ({ page }) => {
  const stamp = String(Date.now()).slice(-6);
  const slug = `e2e-${stamp}`;
  const business = `E2E Consultancy ${stamp}`;
  const adminPhone = freshPhone();
  const adminName = freshName(adminPhone);

  await test.step("the platform admin signs in with username and password", async () => {
    await page.goto("/platform");
    await expect(page).toHaveURL(/\/platform\/login/);
    await page.getByLabel("Username").fill("softzenith");
    await page.getByLabel("Password").fill("local-platform-admin");
    await page.getByRole("button", { name: "Sign in" }).click();
    await expect(page.getByRole("heading", { name: "Businesses" })).toBeVisible();
  });

  await test.step("fills in the business, its enquiry form, a branch and its admin", async () => {
    await page.getByRole("link", { name: "Onboard a business" }).click();
    await page.getByLabel("Business name *").fill(business);
    await page.getByLabel("Web address *").fill(slug);
    await page.getByRole("button", { name: /^Next/ }).click();

    await page.getByLabel(/Lead number prefix/).fill("EEE");
    await page.getByLabel(/Services offered/).fill("Study Visa\nIELTS Coaching");
    await page.getByLabel(/Countries/).fill("Canada\nAustralia");
    await page.getByRole("button", { name: /^Next/ }).click();

    await page.getByRole("button", { name: "Add branch" }).click();
    await page.getByLabel("Branch 1 name").fill("Head Office");
    await page.getByLabel("Branch 1 city").fill("Rohtak");
    await page.getByRole("button", { name: /^Next/ }).click();

    await page.getByRole("button", { name: "Add person" }).click();
    await page.getByLabel("Row 1 name").fill(adminName);
    await page.getByLabel("Row 1 mobile").fill(adminPhone);
    await page.getByLabel("Row 1 email").fill(`owner-${stamp}@example.test`);
    await page.getByLabel("Row 1 branch").selectOption("Head Office");
  });

  await test.step("checks the setup and goes live", async () => {
    await page.getByRole("button", { name: /^Next/ }).click();
    await expect(page.getByText("EEE-000001")).toBeVisible();
    page.once("dialog", (dialog) => dialog.accept());
    await page.getByRole("button", { name: "Go live", exact: true }).click();
    await expect(page.getByRole("heading", { name: `${business} is live` })).toBeVisible();
    await page.getByRole("link", { name: "Back to businesses" }).click();
    await expect(page.getByRole("cell", { name: business })).toBeVisible();
  });

  await test.step("a student enquires on the new business's form", async () => {
    await page.goto(`/enquiry/${slug}`);
    await expect(page.getByText(business, { exact: false }).first()).toBeVisible();
    await page.getByLabel("Full name *").fill("First Student");
    await page.getByLabel("Mobile number *").fill(freshPhone());
    await page.getByLabel("Interested in").selectOption("Study Visa");
    await page.getByRole("button", { name: "Submit enquiry" }).click();
    await expect(page.getByRole("heading", { name: /will get in touch/ })).toBeVisible();
  });

  await test.step("the invited admin signs in to their own CRM and sees the lead", async () => {
    await signIn(page, adminPhone);
    await expect(page.getByText(business, { exact: false }).first()).toBeVisible();
    await page.goto("/leads?q=First Student");
    await expect(page.getByText("EEE-000001")).toBeVisible();
  });
});
