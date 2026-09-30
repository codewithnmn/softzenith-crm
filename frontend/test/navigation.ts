import { vi } from "vitest";

/**
 * Stand-in for next/navigation in component tests. Use with
 * `vi.mock("next/navigation", () => import("@/test/navigation"))` and read `router` / set `nav.path` in the test.
 */
export const router = { push: vi.fn(), replace: vi.fn(), back: vi.fn(), refresh: vi.fn(), prefetch: vi.fn() };
export const nav = { path: "/", search: new URLSearchParams(), params: {} as Record<string, string> };

export const useRouter = () => router;
export const usePathname = () => nav.path;
export const useSearchParams = () => nav.search;
export const useParams = () => nav.params;
