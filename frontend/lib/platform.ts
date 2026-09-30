import createClient, { type Middleware } from "openapi-fetch";
import type { paths } from "./api-schema";

/**
 * The SoftZenith platform console's own session and API client. Deliberately separate from the staff `api` client and
 * `session`: a staff token is never sent to /api/platform, and a platform token never to the staff API.
 */
const TOKEN = "crm.platform.token";
const USER = "crm.platform.user";
const read = (key: string) => (typeof window === "undefined" ? null : localStorage.getItem(key));

export const platformSession = {
  token: () => read(TOKEN),
  username: () => read(USER),
  set: (token: string, username: string) => {
    localStorage.setItem(TOKEN, token);
    localStorage.setItem(USER, username);
  },
  clear: () => {
    localStorage.removeItem(TOKEN);
    localStorage.removeItem(USER);
  },
};

const auth: Middleware = {
  onRequest({ request }) {
    const token = platformSession.token();
    if (token && !new URL(request.url).pathname.endsWith("/auth/login")) request.headers.set("Authorization", `Bearer ${token}`);
    return request;
  },
  onResponse({ response }) {
    if (response.status === 401 && typeof window !== "undefined" && !window.location.pathname.startsWith("/platform/login")) {
      platformSession.clear();
      window.location.href = "/platform/login";
    }
    return response;
  },
};

export const platformApi = createClient<paths>({ baseUrl: "" });
platformApi.use(auth);
