import { defineConfig, devices } from "@playwright/test";

const baseURL = "http://127.0.0.1:4173";

export default defineConfig({
  expect: {
    timeout: 10_000,
  },
  fullyParallel: false,
  reporter: process.env.CI ? "github" : "list",
  testDir: "./e2e",
  timeout: 60_000,
  use: {
    baseURL,
    trace: "retain-on-failure",
  },
  webServer: {
    command: "npm run dev -- --host 127.0.0.1 --port 4173 --mode e2e",
    env: {
      VITE_API_BASE_URL: "http://localhost:8080",
      VITE_E2E_AUTH_DISPLAY_NAME: "E2E Admin",
      VITE_E2E_AUTH_ENABLED: "true",
      VITE_E2E_AUTH_ROLES: "ADMIN,CUSTOMER,AGENT,MANAGER",
      VITE_E2E_AUTH_USER_ID: "00000000-0000-4000-8000-000000000056",
      VITE_E2E_AUTH_USERNAME: "e2e.admin",
      VITE_KEYCLOAK_CLIENT_ID: "ticket-web",
      VITE_KEYCLOAK_REALM: "ticket-management",
      VITE_KEYCLOAK_URL: "http://localhost:18080",
    },
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
    url: baseURL,
  },
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
  ],
});
