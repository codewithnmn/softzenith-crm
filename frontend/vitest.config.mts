import { fileURLToPath } from "node:url";
import { defineConfig } from "vitest/config";

// Unit + component tests (jsdom). Browser journeys against the real backend are Playwright: e2e/, `npm run e2e`.
export default defineConfig({
  oxc: { jsx: { runtime: "automatic" } },
  resolve: { alias: { "@": fileURLToPath(new URL(".", import.meta.url)) } },
  test: {
    environment: "jsdom",
    environmentOptions: { jsdom: { url: "http://localhost:3000/" } },
    setupFiles: ["./test/setup.ts"],
    include: ["**/*.test.{ts,tsx}"],
    exclude: ["node_modules/**", ".next/**", "e2e/**"],
    coverage: { include: ["app/**", "components/**", "lib/**"], exclude: ["lib/api-schema.d.ts", "components/ui/**"] },
  },
});
