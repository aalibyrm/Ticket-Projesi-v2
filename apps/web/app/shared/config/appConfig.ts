import { z } from "zod";

const envSchema = z.object({
  VITE_API_BASE_URL: z.string().url().default("http://localhost:8080"),
  VITE_KEYCLOAK_URL: z.string().url().default("http://localhost:18080"),
  VITE_KEYCLOAK_REALM: z.string().min(1).default("ticket-management"),
  VITE_KEYCLOAK_CLIENT_ID: z.string().min(1).default("ticket-web"),
});

const env = envSchema.parse(import.meta.env);

export const appConfig = {
  apiBaseUrl: env.VITE_API_BASE_URL,
  keycloakClientId: env.VITE_KEYCLOAK_CLIENT_ID,
  keycloakRealm: env.VITE_KEYCLOAK_REALM,
  keycloakUrl: env.VITE_KEYCLOAK_URL,
};
