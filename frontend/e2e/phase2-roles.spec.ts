import { expect, test } from "@playwright/test";
import { signIn, staff } from "./helpers";

/** Phase 2 in the browser: each role's workspace shows its own menu and only the leads its data scope allows. */

test("admin sees every branch, the dashboard and the admin pages", async ({ page }) => {
  await signIn(page, staff.anuj);
  await expect(page).toHaveURL(/\/dashboard/);
  await expect(page.getByText("All branches").first()).toBeVisible();
  for (const item of ["Dashboard", "Leads", "Staff", "Branches"]) {
    await expect(page.getByRole("link", { name: item, exact: true }).first()).toBeVisible();
  }
  await page.goto("/leads?q=Sneha");
  await expect(page.getByRole("link", { name: "Sneha Gupta" })).toBeVisible(); // Rohini
  await page.goto("/leads?q=Rohit Malik");
  await expect(page.getByRole("link", { name: "Rohit Malik" })).toBeVisible(); // Rohtak
});

test("a branch manager sees their branch's leads only", async ({ page }) => {
  await signIn(page, staff.naman);
  await expect(page).toHaveURL(/\/leads/); // no dashboard for this role
  await page.goto("/leads?q=Sneha");
  await expect(page.getByRole("link", { name: "Sneha Gupta" })).toBeVisible(); // Rohini
  await page.goto("/leads?q=Rohit Malik");
  await expect(page.getByText("No leads match these filters.")).toBeVisible(); // Rohtak
  await expect(page.getByRole("link", { name: "Branches", exact: true })).toHaveCount(0);
});

test("a counsellor sees only the leads assigned to them and no admin menu", async ({ page }) => {
  await signIn(page, staff.natasha);
  await page.goto("/leads?q=Aarav");
  await expect(page.getByRole("link", { name: "Aarav Sharma" })).toBeVisible(); // assigned to Natasha
  await page.goto("/leads?q=Rohit Malik");
  await expect(page.getByText("No leads match these filters.")).toBeVisible(); // unassigned
  await expect(page.getByRole("link", { name: "Dashboard", exact: true })).toHaveCount(0);
  await page.goto("/dashboard");
  await expect(page).toHaveURL(/\/leads/);
});

test("signing out returns to the login page and protects the staff area", async ({ page }) => {
  await signIn(page, staff.frontDesk);
  await page.getByText("Front Desk").first().click();
  await page.getByText("Sign out").click();
  await expect(page).toHaveURL(/\/login/);
  await page.goto("/leads");
  await expect(page).toHaveURL(/\/login/);
});
