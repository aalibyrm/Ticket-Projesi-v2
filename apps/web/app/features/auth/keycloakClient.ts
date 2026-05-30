import type Keycloak from "keycloak-js";
import { appConfig } from "~/shared/config/appConfig";

let keycloak: Keycloak | undefined;

export async function getKeycloakClient() {
  if (!keycloak) {
    const KeycloakConstructor = (await import("keycloak-js")).default;
    keycloak = new KeycloakConstructor({
      clientId: appConfig.keycloakClientId,
      realm: appConfig.keycloakRealm,
      url: appConfig.keycloakUrl,
    });
  }

  return keycloak;
}
