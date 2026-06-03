import { mobileEnv } from "../config/env";
import type { AppRole, AuthUser } from "./authTypes";

interface JwtClaims {
  email?: string;
  name?: string;
  preferred_username?: string;
  realm_access?: {
    roles?: string[];
  };
  resource_access?: Record<string, { roles?: string[] }>;
  sub?: string;
}

const knownRoles: AppRole[] = ["CUSTOMER", "AGENT", "MANAGER", "ADMIN"];

export function getAuthUserFromAccessToken(accessToken: string | undefined): AuthUser | undefined {
  const claims = decodeJwtPayload(accessToken);

  if (!claims?.sub) {
    return undefined;
  }

  const roles = extractRoles(claims);
  const username = claims.preferred_username || claims.email || claims.sub;

  return {
    displayName: claims.name || username,
    id: claims.sub,
    roles,
    username
  };
}

export function extractRoles(claims: JwtClaims): AppRole[] {
  const realmRoles = claims.realm_access?.roles ?? [];
  const clientRoles = claims.resource_access?.[mobileEnv.keycloakClientId]?.roles ?? [];
  const allRoles = [...realmRoles, ...clientRoles]
    .map((role) => role.toUpperCase())
    .map((role) => role.replace(/^ROLE_/, ""))
    .filter((role): role is AppRole => knownRoles.includes(role as AppRole));

  return [...new Set(allRoles)];
}

function decodeJwtPayload(accessToken: string | undefined): JwtClaims | undefined {
  const payload = accessToken?.split(".")[1];

  if (!payload) {
    return undefined;
  }

  try {
    return JSON.parse(decodeUtf8(base64UrlDecode(payload))) as JwtClaims;
  } catch {
    return undefined;
  }
}

function base64UrlDecode(value: string) {
  const normalized = value.replace(/-/g, "+").replace(/_/g, "/");
  return base64Decode(normalized);
}

function base64Decode(value: string) {
  if (typeof globalThis.atob === "function") {
    return globalThis.atob(value);
  }

  const alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
  let bits = 0;
  let buffer = 0;
  let output = "";

  for (const char of value.replace(/=+$/, "")) {
    const index = alphabet.indexOf(char);

    if (index < 0) {
      continue;
    }

    buffer = (buffer << 6) | index;
    bits += 6;

    if (bits >= 8) {
      bits -= 8;
      output += String.fromCharCode((buffer >> bits) & 0xff);
    }
  }

  return output;
}

function decodeUtf8(binary: string) {
  try {
    return decodeURIComponent(
      binary
        .split("")
        .map((char) => `%${char.charCodeAt(0).toString(16).padStart(2, "0")}`)
        .join("")
    );
  } catch {
    return binary;
  }
}
