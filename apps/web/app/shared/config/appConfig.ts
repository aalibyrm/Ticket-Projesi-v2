import { z } from "zod";

const envSchema = z.object({
  VITE_API_BASE_URL: z.string().url().default("http://localhost:8080"),
  VITE_E2E_AUTH_DISPLAY_NAME: z.string().min(1).default("E2E User"),
  VITE_E2E_AUTH_ENABLED: z.enum(["false", "true"]).default("false"),
  VITE_E2E_AUTH_ROLES: z.string().min(1).default("CUSTOMER"),
  VITE_E2E_AUTH_USER_ID: z.string().min(1).default("00000000-0000-4000-8000-000000000000"),
  VITE_E2E_AUTH_USERNAME: z.string().min(1).default("e2e.user"),
  VITE_KEYCLOAK_URL: z.string().url().default("http://localhost:18080"),
  VITE_KEYCLOAK_REALM: z.string().min(1).default("ticket-management"),
  VITE_KEYCLOAK_CLIENT_ID: z.string().min(1).default("ticket-web"),
});

const env = envSchema.parse(import.meta.env);

export const appConfig = {
  apiBaseUrl: env.VITE_API_BASE_URL,
  e2eAuthDisplayName: env.VITE_E2E_AUTH_DISPLAY_NAME,
  e2eAuthEnabled: !import.meta.env.PROD && env.VITE_E2E_AUTH_ENABLED === "true",
  e2eAuthRoles: env.VITE_E2E_AUTH_ROLES,
  e2eAuthUserId: env.VITE_E2E_AUTH_USER_ID,
  e2eAuthUsername: env.VITE_E2E_AUTH_USERNAME,
  keycloakClientId: env.VITE_KEYCLOAK_CLIENT_ID,
  keycloakRealm: env.VITE_KEYCLOAK_REALM,
  keycloakUrl: env.VITE_KEYCLOAK_URL,
};
