import type { AuthTokens } from "./authTypes";

const EXPIRY_SKEW_MS = 30000;

export function isTokenUsable(tokens: AuthTokens | undefined, now = Date.now()) {
  if (!tokens?.accessToken) {
    return false;
  }

  if (!tokens.expiresAt) {
    return true;
  }

  return tokens.expiresAt - EXPIRY_SKEW_MS > now;
}
