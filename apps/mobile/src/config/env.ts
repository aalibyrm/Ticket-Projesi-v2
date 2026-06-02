export interface MobileEnv {
  apiBaseUrl: string;
  keycloakIssuerUrl: string;
  keycloakClientId: string;
  authRedirectScheme: string;
}

function readPublicEnv(name: string, fallback: string) {
  const value = process.env[name];
  return value && value.trim().length > 0 ? value.trim() : fallback;
}

export const mobileEnv: MobileEnv = Object.freeze({
  apiBaseUrl: readPublicEnv("EXPO_PUBLIC_API_BASE_URL", "http://localhost:8080"),
  keycloakIssuerUrl: readPublicEnv(
    "EXPO_PUBLIC_KEYCLOAK_ISSUER_URL",
    "http://localhost:8080/realms/ticket-management"
  ),
  keycloakClientId: readPublicEnv("EXPO_PUBLIC_KEYCLOAK_CLIENT_ID", "ticket-mobile"),
  authRedirectScheme: readPublicEnv("EXPO_PUBLIC_AUTH_REDIRECT_SCHEME", "ticketv2")
});

export function isAuthConfigured(env: MobileEnv = mobileEnv) {
  return Boolean(env.keycloakIssuerUrl && env.keycloakClientId && env.authRedirectScheme);
}

export function getMissingAuthConfig(env: MobileEnv = mobileEnv) {
  return [
    ["EXPO_PUBLIC_KEYCLOAK_ISSUER_URL", env.keycloakIssuerUrl],
    ["EXPO_PUBLIC_KEYCLOAK_CLIENT_ID", env.keycloakClientId],
    ["EXPO_PUBLIC_AUTH_REDIRECT_SCHEME", env.authRedirectScheme]
  ]
    .filter(([, value]) => !value)
    .map(([name]) => name);
}
