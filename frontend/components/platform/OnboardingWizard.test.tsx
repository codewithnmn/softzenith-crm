import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { json, requests, routes } from "@/test/api-mock";
import type { Blueprint } from "@/lib/blueprint";
import { platformSession } from "@/lib/platform";
import { OnboardingWizard } from "./OnboardingWizard";

const READY_PLAN = {
  ready: true, problems: [], warnings: ["Nobody gets alerts"],
  summary: {
    name: "Western World Visa Services", slug: "western-world-visa-services", defaultRegion: "IN", timezone: "Asia/Kolkata",
    leadNumberExample: "WWV-000001", publicFormPath: "/enquiry/western-world-visa-services",
    roles: ["Admin", "Branch Manager", "Counsellor", "Receptionist"], branches: ["Rohtak (Rohtak)"],
    staff: [{ fullName: "Anuj Kumar", phone: "+919876543210", role: "Admin", branch: "Rohtak" }], features: ["LEADS_CORE", "TEAM_AND_STUDENTS"],
  },
};

const next = () => userEvent.click(screen.getByRole("button", { name: /^Next/ }));
const posted = (path: string) => requests.filter((r) => r.method === "POST" && r.path === path);

async function fillWesternWorld() {
  await userEvent.type(screen.getByRole("textbox", { name: /business name/i }), "Western World Visa Services");
  expect(screen.getByRole("textbox", { name: /web address/i })).toHaveValue("western-world-visa-services");
  await next();

  await userEvent.type(screen.getByRole("textbox", { name: /lead number prefix/i }), "wwv");
  await userEvent.type(screen.getByRole("textbox", { name: /services offered/i }), "Study Visa{enter}IELTS Coaching");
  await userEvent.click(screen.getByRole("textbox", { name: /countries/i })); // blur commits the services
  await next();

  await userEvent.click(screen.getByRole("button", { name: "Add branch" }));
  await userEvent.type(screen.getByRole("textbox", { name: "Branch 1 name" }), "Rohtak");
  await userEvent.type(screen.getByRole("textbox", { name: "Branch 1 city" }), "Rohtak");
  await next();

  await userEvent.click(screen.getByRole("button", { name: "Add person" }));
  await userEvent.type(screen.getByRole("textbox", { name: "Row 1 name" }), "Anuj Kumar");
  await userEvent.type(screen.getByRole("textbox", { name: "Row 1 mobile" }), "9876543210");
  await userEvent.selectOptions(screen.getByRole("combobox", { name: "Row 1 branch" }), "Rohtak");
}

describe("Onboarding a business", () => {
  beforeEach(() => {
    platformSession.set("pt-1", "softzenith");
  });

  it("walks through the steps, checks the setup and goes live", async () => {
    routes({
      "POST /api/platform/tenants/preview": () => json(READY_PLAN),
      "POST /api/platform/tenants": () => json({ tenantId: "t1", slug: "western-world-visa-services", name: "Western World Visa Services", branches: 1, staff: 1 }, 201),
    });
    vi.spyOn(window, "confirm").mockReturnValue(true);
    render(<OnboardingWizard />);

    await fillWesternWorld();
    await next(); // → Review: checks automatically

    expect(await screen.findByText("WWV-000001")).toBeInTheDocument();
    expect(screen.getByText("Nobody gets alerts")).toBeInTheDocument();
    const sent = posted("/api/platform/tenants/preview")[0].body as Blueprint;
    expect(sent.business).toMatchObject({ name: "Western World Visa Services", slug: "western-world-visa-services", defaultRegion: "IN" });
    expect(sent.settings).toMatchObject({ leadNumberPrefix: "WWV", serviceInterests: ["Study Visa", "IELTS Coaching"] });
    expect(sent.branches).toEqual([{ name: "Rohtak", city: "Rohtak" }]);
    expect(sent.staff![0]).toMatchObject({ fullName: "Anuj Kumar", phone: "9876543210", role: "Admin", branch: "Rohtak" });

    await userEvent.click(screen.getByRole("button", { name: "Go live" }));
    expect(await screen.findByRole("heading", { name: "Western World Visa Services is live" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "/enquiry/western-world-visa-services" })).toBeInTheDocument();
    expect(posted("/api/platform/tenants")).toHaveLength(1);
    expect(localStorage.getItem("crm.platform.onboarding-draft")).toBeNull();
  }, 20_000);

  it("shows problems on the step they belong to and never offers go live", async () => {
    routes({
      "POST /api/platform/tenants/preview": () => json({
        ready: false, warnings: [],
        problems: [{ field: "staff[0].phone", message: "Anuj: '12' is not a valid mobile number" }, { field: "business.slug", message: "Web address 'x' is already in use" }],
      }),
    });
    render(<OnboardingWizard />);
    await userEvent.click(screen.getByRole("button", { name: /Review & go live/ }));

    expect(await screen.findByText(/not a valid mobile number/)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Go live" })).toBeDisabled();
    const steps = screen.getByRole("navigation", { name: "Onboarding steps" });
    expect(within(steps).getAllByLabelText("1 problem")).toHaveLength(2); // Business and Staff

    await userEvent.click(screen.getByRole("button", { name: "Go to Staff" }));
    expect(screen.getByRole("heading", { name: "Staff" })).toBeInTheDocument();
    expect(screen.getByText(/not a valid mobile number/)).toBeInTheDocument();
  });

  it("asks for a new check after any change", async () => {
    routes({ "POST /api/platform/tenants/preview": () => json(READY_PLAN) });
    render(<OnboardingWizard />);
    await fillWesternWorld();
    await next();
    await screen.findByText("WWV-000001");

    await userEvent.click(screen.getByRole("button", { name: /Business/ }));
    await userEvent.type(screen.getByRole("textbox", { name: /business name/i }), " Pvt Ltd");
    await userEvent.click(screen.getByRole("button", { name: /Review & go live/ }));

    await vi.waitFor(() => expect(posted("/api/platform/tenants/preview")).toHaveLength(2));
  }, 20_000);

  it("adds staff pasted from a spreadsheet", async () => {
    render(<OnboardingWizard />);
    await userEvent.click(screen.getByRole("button", { name: /^4/ }));
    await userEvent.click(screen.getByText("Paste from a spreadsheet"));
    await userEvent.click(screen.getByRole("textbox", { name: /rows to add/i }));
    await userEvent.paste("Name\tMobile\tEmail\tRole\nAnuj\t9876543210\tanuj@ww.test\tAdmin\nPriya\t9876543211\t\tCounsellor");
    await userEvent.click(screen.getByRole("button", { name: /add these rows/i }));

    expect(screen.getByRole("textbox", { name: "Row 2 name" })).toHaveValue("Priya");
    expect(screen.getByRole("combobox", { name: "Row 1 role" })).toHaveValue("Admin");
  });

  it("keeps the draft in this browser and can load a saved file", async () => {
    const { unmount } = render(<OnboardingWizard />);
    await userEvent.type(screen.getByRole("textbox", { name: /business name/i }), "Draft Co");
    unmount();
    render(<OnboardingWizard />);
    expect(screen.getByRole("textbox", { name: /business name/i })).toHaveValue("Draft Co");

    const file = new File([JSON.stringify({ business: { name: "From File", slug: "from-file" } })], "ww.blueprint.json", { type: "application/json" });
    await userEvent.upload(screen.getByLabelText("Load a saved onboarding file"), file);
    expect(await screen.findByDisplayValue("From File")).toBeInTheDocument();
    expect(screen.getByRole("textbox", { name: /web address/i })).toHaveValue("from-file");
  });
});
