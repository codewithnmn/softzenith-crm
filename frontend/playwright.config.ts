import { defineConfig, devices } from "@playwright/test";

/**
 * Browser journeys against the real app: Next.js UI (:3000) + Spring Boot API (:8081, dev profile, seeded Demo Visas
 * organisation, dev login). Postgres must be running (see CLAUDE.md). Servers already running (e.g. `.\dev`) are reused.
 * Run: `npm run e2e`   Debug one step at a time: `npx playwright test --debug`   Report: `npx playwright show-report`
 */
export default defineConfig({
  testDir: "./e2e",
  fullyParallel: false, // the journeys share one seeded database
  workers: 1,
  retries: 0,
  timeout: 60_000,
  reporter: [["list"], ["html", { open: "never" }]],
  use: {
    baseURL: "http://localhost:3000",
    trace: "retain-on-failure",
    screenshot: "only-on-failure",
  },
  projects: [{ name: "chromium", use: { ...devices["Desktop Chrome"] } }],
  webServer: [
    {
      command: process.platform === "win32"
        ? ".\\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev"
        : "./mvnw spring-boot:run -Dspring-boot.run.profiles=dev",
      cwd: "../backend",
      url: "http://localhost:8081/actuator/health",
      reuseExistingServer: true,
      timeout: 240_000,
    },
    {
      command: "npm run dev",
      url: "http://localhost:3000/login",
      reuseExistingServer: true,
      timeout: 120_000,
    },
  ],
});
