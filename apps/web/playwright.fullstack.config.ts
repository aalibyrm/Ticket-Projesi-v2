import { defineConfig, devices } from "@playwright/test";

const baseURL = process.env.FULLSTACK_WEB_BASE_URL ?? "http://localhost:5173";
const apiBaseUrl = process.env.VITE_API_BASE_URL ?? "http://localhost:8088";
const keycloakUrl = process.env.VITE_KEYCLOAK_URL ?? "http://localhost:8080";

export default defineConfig({
  expect: {
    timeout: 15_000,
  },
  fullyParallel: false,
  reporter: process.env.CI ? "github" : "list",
  testDir: "./e2e/fullstack",
  timeout: 120_000,
  use: {
    baseURL,
    trace: "retain-on-failure",
  },
  webServer: {
    command: "npm run dev -- --host 127.0.0.1 --port 5173",
    env: {
      VITE_API_BASE_URL: apiBaseUrl,
      VITE_KEYCLOAK_CLIENT_ID: process.env.VITE_KEYCLOAK_CLIENT_ID ?? "ticket-web",
      VITE_KEYCLOAK_REALM: process.env.VITE_KEYCLOAK_REALM ?? "ticket-management",
      VITE_KEYCLOAK_URL: keycloakUrl,
      VITE_E2E_AUTH_ENABLED: "false",
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
