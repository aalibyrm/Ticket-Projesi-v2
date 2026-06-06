import type { AppRole, AuthUser } from "~/features/auth/authTypes";
import {
  getE2eAccessToken,
  getE2eAuthUser,
  isE2eAuthEnabled,
} from "~/features/auth/e2eAuth";
import { getKeycloakClient } from "~/features/auth/keycloakClient";

const allowedRoles: AppRole[] = ["CUSTOMER", "AGENT", "MANAGER", "ADMIN"];

export async function initializeAuth() {
  const e2eUser = getE2eAuthUser();
  if (e2eUser) {
    return e2eUser;
  }

  const keycloak = await getKeycloakClient();
  const authenticated = await keycloak.init({
    checkLoginIframe: false,
    onLoad: "check-sso",
    pkceMethod: "S256",
    silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
  });

  return authenticated ? toAuthUser(keycloak.tokenParsed) : undefined;
}

export async function login() {
  if (isE2eAuthEnabled()) {
    return;
  }

  const keycloak = await getKeycloakClient();
  await keycloak.login({
    redirectUri: window.location.origin,
  });
}

export async function logout() {
  if (isE2eAuthEnabled()) {
    return;
  }

  const keycloak = await getKeycloakClient();
  await keycloak.logout({
    redirectUri: window.location.origin,
  });
}

export async function getAccessToken() {
  const e2eToken = getE2eAccessToken();
  if (e2eToken) {
    return e2eToken;
  }

  const keycloak = await getKeycloakClient();
  if (!keycloak.authenticated) {
    return undefined;
  }

  await keycloak.updateToken(30);
  return keycloak.token;
}

function toAuthUser(tokenParsed: unknown): AuthUser {
  const token = tokenParsed as {
    name?: string;
    preferred_username?: string;
    realm_access?: { roles?: string[] };
    sub?: string;
  };
  const roles = token.realm_access?.roles?.filter(isAppRole) ?? [];
  const username = token.preferred_username ?? "unknown";

  return {
    displayName: token.name ?? username,
    id: token.sub ?? username,
    roles,
    username,
  };
}

function isAppRole(role: string): role is AppRole {
  return allowedRoles.includes(role as AppRole);
}
