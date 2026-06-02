import { getMissingAuthConfig, isAuthConfigured, type MobileEnv } from "./env";

const baseEnv: MobileEnv = {
  apiBaseUrl: "http://localhost:8080",
  keycloakIssuerUrl: "http://localhost:8080/realms/ticket-management",
  keycloakClientId: "ticket-mobile",
  authRedirectScheme: "ticketv2"
};

describe("mobile env", () => {
  it("accepts complete auth config", () => {
    expect(isAuthConfigured(baseEnv)).toBe(true);
    expect(getMissingAuthConfig(baseEnv)).toEqual([]);
  });

  it("reports missing auth config keys", () => {
    const missingEnv = { ...baseEnv, keycloakClientId: "" };

    expect(isAuthConfigured(missingEnv)).toBe(false);
    expect(getMissingAuthConfig(missingEnv)).toEqual(["EXPO_PUBLIC_KEYCLOAK_CLIENT_ID"]);
  });
});
