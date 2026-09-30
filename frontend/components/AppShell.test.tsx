import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { json, mockApi } from "@/test/api-mock";
import { nav, router } from "@/test/navigation";
import { renderAsStaff, roles } from "@/test/staff";
import AppShell, { can } from "./AppShell";
import { render } from "@testing-library/react";
import { session } from "@/lib/session";

vi.mock("next/navigation", () => import("@/test/navigation"));

const menu = () => screen.getAllByRole("link").map((a) => a.textContent);

describe("AppShell", () => {
  beforeEach(() => {
    nav.path = "/leads";
  });

  it("shows admins every module", async () => {
    await renderAsStaff(<p>page</p>, roles.admin);
    expect(menu()).toEqual(expect.arrayContaining(["Dashboard", "Leads", "Staff", "Branches"]));
    expect(screen.getByText("All branches")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /add lead/i })).toBeInTheDocument();
  });

  it("shows a counsellor only what their role allows, and their branch", async () => {
    await renderAsStaff(<p>page</p>, roles.counsellor);
    expect(menu()).toContain("Leads");
    expect(menu()).not.toContain("Dashboard");
    expect(menu()).not.toContain("Branches");
    expect(screen.getByText("Rohini")).toBeInTheDocument();
  });

  it("marks the current page in the menu", async () => {
    await renderAsStaff(<p>page</p>, roles.receptionist);
    expect(screen.getByRole("link", { name: "Leads" })).toHaveAttribute("aria-current", "page");
    expect(screen.getByRole("link", { name: "Staff" })).not.toHaveAttribute("aria-current");
  });

  it("searches leads from the top bar", async () => {
    await renderAsStaff(<p>page</p>, roles.receptionist);
    await userEvent.type(screen.getByPlaceholderText(/search leads/i), "  Riya {enter}");
    expect(router.push).toHaveBeenCalledWith("/leads?q=Riya");
  });

  it("sends people who are not signed in to the login page", () => {
    render(<AppShell><p>page</p></AppShell>);
    expect(router.replace).toHaveBeenCalledWith("/login");
  });

  it("sends people whose sign-in the backend refuses to the login page", async () => {
    session.setToken("stale");
    mockApi(() => json({ detail: "Not staff here" }, 403));
    render(<AppShell><p>page</p></AppShell>);
    await vi.waitFor(() => expect(router.replace).toHaveBeenCalledWith("/login"));
  });

  it("signs out", async () => {
    await renderAsStaff(<p>page</p>, roles.admin);
    await userEvent.click(screen.getAllByText("Anuj Admin")[0]);
    await userEvent.click(await screen.findByText("Sign out"));
    expect(session.token()).toBeNull();
    expect(router.replace).toHaveBeenCalledWith("/login");
  });
});

describe("can", () => {
  it("is true only for permissions the user holds", () => {
    expect(can(roles.receptionist, "LEAD_ASSIGN")).toBe(true);
    expect(can(roles.receptionist, "LEAD_REOPEN")).toBe(false);
    expect(can({}, "LEAD_VIEW")).toBe(false);
  });
});
