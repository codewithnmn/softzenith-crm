import { describe, expect, it } from "vitest";
import { json, mockApi, requests } from "@/test/api-mock";
import { api, errorMessage, formatDate, unwrap } from "./api";
import { session } from "./session";

describe("errorMessage", () => {
  it("prefers the problem detail from the backend", () => {
    expect(errorMessage({ detail: "Lead is already CLOSED" })).toBe("Lead is already CLOSED");
  });
  it("falls back to an Error's message, then a generic text", () => {
    expect(errorMessage(new Error("offline"))).toBe("offline");
    expect(errorMessage({ detail: "" })).toBe("Something went wrong");
    expect(errorMessage(undefined)).toBe("Something went wrong");
  });
});

describe("unwrap", () => {
  it("returns the data of a successful call", async () => {
    await expect(unwrap(Promise.resolve({ data: [1, 2] }))).resolves.toEqual([1, 2]);
  });
  it("throws the problem detail of a failed call", async () => {
    await expect(unwrap(Promise.resolve({ error: { detail: "Not allowed" } }))).rejects.toThrow("Not allowed");
    await expect(unwrap(Promise.resolve({}))).rejects.toThrow("Something went wrong");
  });
});

describe("formatDate", () => {
  it("formats an ISO timestamp and leaves blanks empty", () => {
    expect(formatDate("2026-09-30T10:15:00Z")).toMatch(/2026/);
    expect(formatDate(null)).toBe("");
    expect(formatDate(undefined)).toBe("");
  });
});

describe("the api client", () => {
  it("sends the signed-in token and the chosen tenant on staff calls", async () => {
    session.setToken("tok-1");
    session.setTenant("tenant-1");
    mockApi(() => json({ fullName: "Anuj" }));

    await api.GET("/api/v1/me");

    expect(requests[0].headers.get("Authorization")).toBe("Bearer tok-1");
    expect(requests[0].headers.get("X-Tenant-ID")).toBe("tenant-1");
  });

  it("never sends a (possibly stale) token to public endpoints", async () => {
    session.setToken("tok-1");
    mockApi(() => json({ tenantName: "Acme", branches: [] }));

    await api.GET("/api/v1/public/tenants/{slug}/enquiry-form", { params: { path: { slug: "acme" } } });

    expect(requests[0].headers.get("Authorization")).toBeNull();
  });

  it("signs the user out and goes to /login when the backend answers 401", async () => {
    session.setToken("expired");
    window.history.pushState({}, "", "/leads");
    mockApi(() => json({ detail: "Unauthorized" }, 401));

    await api.GET("/api/v1/leads");

    expect(session.token()).toBeNull();
  });

  it("does not redirect a 401 on the login or public enquiry pages", async () => {
    session.setToken("keep");
    window.history.pushState({}, "", "/enquiry/acme");
    mockApi(() => json({ detail: "Unauthorized" }, 401));

    await api.GET("/api/v1/leads");

    expect(session.token()).toBe("keep");
  });
});
