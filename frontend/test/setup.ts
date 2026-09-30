import "@testing-library/jest-dom/vitest";
import { cleanup } from "@testing-library/react";
import { afterEach, vi } from "vitest";
import { fakeFetch, resetApi } from "./api-mock";

// Must happen before any test imports lib/api (it captures fetch at import time).
globalThis.fetch = fakeFetch;

// openapi-fetch builds `new Request("/api/...")`; Node's Request needs an absolute URL, so resolve against the page.
const NodeRequest = globalThis.Request;
globalThis.Request = class extends NodeRequest {
  constructor(input: RequestInfo | URL, init?: RequestInit) {
    super(typeof input === "string" && input.startsWith("/") ? new URL(input, window.location.origin) : input, init);
  }
} as typeof Request;

afterEach(() => {
  cleanup();
  localStorage.clear();
  resetApi();
  vi.clearAllMocks();
});
