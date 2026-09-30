import createClient, { type Middleware } from "openapi-fetch";
import type { components, paths } from "./api-schema";
import { session } from "./session";

/** Types generated from the backend's OpenAPI spec (`npm run gen:api`). */
export type Schemas = components["schemas"];

/** Endpoints that never need (or want) a stale signed-in token attached; mirrors the backend's permitAll list. */
const isPublicPath = (path: string) =>
  path.startsWith("/api/v1/public/") || path.startsWith("/api/v1/dev/") || path.startsWith("/api/v1/webhooks/");

const auth: Middleware = {
  async onRequest({ request }) {
    if (isPublicPath(new URL(request.url).pathname)) return request;
    const token = await session.accessToken();
    if (token) request.headers.set("Authorization", `Bearer ${token}`);
    const tenant = session.tenant();
    if (tenant) request.headers.set("X-Tenant-ID", tenant);
    return request;
  },
  onResponse({ response }) {
    const path = typeof window === "undefined" ? "" : window.location.pathname;
    if (response.status === 401 && !path.startsWith("/login") && !path.startsWith("/enquiry")) {
      session.clear();
      window.location.href = "/login";
    }
    return response;
  },
};

export const api = createClient<paths>({ baseUrl: "" });
api.use(auth);

/** Human-readable message from an RFC 9457 problem response. */
export function errorMessage(error: unknown): string {
  if (error && typeof error === "object" && "detail" in error && error.detail) return String(error.detail);
  if (error instanceof Error) return error.message;
  return "Something went wrong";
}

/** For SWR fetchers: unwraps an openapi-fetch result or throws its problem detail. */
export async function unwrap<T>(call: Promise<{ data?: T; error?: unknown }>): Promise<T> {
  const { data, error } = await call;
  if (error !== undefined || data === undefined) throw new Error(errorMessage(error));
  return data;
}

export const formatDate = (iso?: string | null) =>
  iso ? new Date(iso).toLocaleString(undefined, { dateStyle: "medium", timeStyle: "short" }) : "";
