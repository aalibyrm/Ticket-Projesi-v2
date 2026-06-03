import type { AppRole, AuthUser } from "~/features/auth/authTypes";
import { appConfig } from "~/shared/config/appConfig";

const allowedRoles: AppRole[] = ["CUSTOMER", "AGENT", "MANAGER", "ADMIN"];

export function isE2eAuthEnabled() {
  return appConfig.e2eAuthEnabled || isBrowserFlagEnabled();
}

export function getE2eAuthUser(): AuthUser | undefined {
  if (!isE2eAuthEnabled()) {
    return undefined;
  }

  return {
    displayName: getBrowserSetting("display-name", appConfig.e2eAuthDisplayName),
    id: getBrowserSetting("user-id", appConfig.e2eAuthUserId),
    roles: parseRoles(getBrowserSetting("roles", appConfig.e2eAuthRoles)),
    username: getBrowserSetting("username", appConfig.e2eAuthUsername),
  };
}

export function getE2eAccessToken() {
  return isE2eAuthEnabled() ? "e2e-local-browser-token" : undefined;
}

function parseRoles(value: string): AppRole[] {
  const roles = value
    .split(",")
    .map((role) => role.trim())
    .filter((role): role is AppRole => allowedRoles.includes(role as AppRole));

  return roles.length > 0 ? roles : ["CUSTOMER"];
}

function isBrowserFlagEnabled() {
  if (import.meta.env.PROD || typeof window === "undefined") {
    return false;
  }

  return window.localStorage.getItem("ticket:e2e-auth") === "enabled";
}

function getBrowserSetting(key: string, fallback: string) {
  if (typeof window === "undefined") {
    return fallback;
  }

  return window.localStorage.getItem(`ticket:e2e-auth:${key}`) ?? fallback;
}
