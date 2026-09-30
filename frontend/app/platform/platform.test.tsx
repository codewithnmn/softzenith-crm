import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { SWRConfig } from "swr";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { json, requests, routes } from "@/test/api-mock";
import { nav, router } from "@/test/navigation";
import { session } from "@/lib/session";
import { platformSession } from "@/lib/platform";
import PlatformShell from "@/components/platform/PlatformShell";
import PlatformLoginPage from "./login/page";
import PlatformHome from "./page";

vi.mock("next/navigation", () => import("@/test/navigation"));

describe("Platform sign-in", () => {
  beforeEach(() => {
    nav.path = "/platform/login";
  });

  it("signs a platform admin in with username and password", async () => {
    routes({ "POST /api/platform/auth/login": () => json({ username: "softzenith", accessToken: "pt-1", expiresAt: "2026-10-01T00:00:00Z" }) });
    render(<PlatformLoginPage />);
    await userEvent.type(screen.getByRole("textbox", { name: /username/i }), "softzenith");
    await userEvent.type(screen.getByLabelText(/password/i), "local-platform-admin");
    await userEvent.click(screen.getByRole("button", { name: "Sign in" }));

    await vi.waitFor(() => expect(router.replace).toHaveBeenCalledWith("/platform"));
    expect(platformSession.token()).toBe("pt-1");
    expect(requests[0].body).toEqual({ username: "softzenith", password: "local-platform-admin" });
  });

  it("shows the refusal and clears the password", async () => {
    routes({ "POST /api/platform/auth/login": () => json({ detail: "Wrong username or password, or the account is locked" }, 401) });
    render(<PlatformLoginPage />);
    await userEvent.type(screen.getByRole("textbox", { name: /username/i }), "softzenith");
    await userEvent.type(screen.getByLabelText(/password/i), "nope-nope-nope");
    await userEvent.click(screen.getByRole("button", { name: "Sign in" }));

    expect(await screen.findByText(/Wrong username or password/)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toHaveValue("");
    expect(platformSession.token()).toBeNull();
  });
});

describe("Platform console", () => {
  beforeEach(() => {
    nav.path = "/platform";
  });

  it("sends visitors without a platform session to its own sign-in, never the staff one", async () => {
    session.setToken("a-staff-token"); // being signed in as staff does not count
    render(<PlatformShell><p>secret</p></PlatformShell>);
    await vi.waitFor(() => expect(router.replace).toHaveBeenCalledWith("/platform/login"));
    expect(screen.queryByText("secret")).toBeNull();
  });

  it("lists the businesses and sends only the platform token", async () => {
    platformSession.set("pt-1", "softzenith");
    session.setToken("a-staff-token");
    routes({
      "GET /api/platform/tenants": () => json([
        { id: "t1", slug: "westernworld", name: "Western World Visa Services", status: "ACTIVE", createdAt: "2026-09-30T10:00:00Z" },
      ]),
    });
    render(
      <SWRConfig value={{ provider: () => new Map() }}>
        <PlatformShell><PlatformHome /></PlatformShell>
      </SWRConfig>,
    );

    expect(await screen.findByText("Western World Visa Services")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /westernworld/ })).toHaveAttribute("href", "/enquiry/westernworld");
    expect(screen.getByText("softzenith")).toBeInTheDocument();
    expect(requests[0].headers.get("Authorization")).toBe("Bearer pt-1");
    expect(requests[0].headers.get("X-Tenant-ID")).toBeNull();
  });

  it("signs out", async () => {
    platformSession.set("pt-1", "softzenith");
    routes({ "GET /api/platform/tenants": () => json([]) });
    render(<SWRConfig value={{ provider: () => new Map() }}><PlatformShell><PlatformHome /></PlatformShell></SWRConfig>);
    await userEvent.click(await screen.findByRole("button", { name: /sign out/i }));
    expect(platformSession.token()).toBeNull();
    expect(router.replace).toHaveBeenCalledWith("/platform/login");
  });
});
