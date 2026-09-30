import { expect, type Page } from "@playwright/test";

/** Dev-seed staff (backend devdata/DevDataSeeder). */
export const staff = {
  anuj: "9000000001", // Admin, Rohtak
  naman: "9000000002", // Branch Manager, Rohini
  natasha: "9000000003", // Counsellor, Rohtak
  frontDesk: "9000000004", // Receptionist
} as const;

export const TENANT = "demo"; // the dev seed tenant

/** Signs in through the login page (dev mode: phone only) and waits for the staff area. */
export async function signIn(page: Page, phone: string) {
  await page.goto("/login");
  await page.getByLabel("Mobile number").fill(phone);
  await page.getByRole("button", { name: "Sign in" }).click();
  await expect(page.getByRole("navigation").first()).toBeVisible(); // the staff sidebar: signed in and /me loaded
}

/** A fresh, valid Indian mobile number so repeated runs never collide with earlier data. */
export function freshPhone() {
  return "98" + String(Date.now()).slice(-8);
}

/** A unique person's name for this run. Names may only contain letters (the backend refuses digits). */
export function freshName(phone: string) {
  const letters = phone.slice(-4).replace(/\d/g, (d) => "abcdefghij"[Number(d)]);
  return `Riya Test${letters}`;
}
