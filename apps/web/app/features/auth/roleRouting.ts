import type { AppRole } from "~/features/auth/authTypes";

const allRoles: AppRole[] = ["CUSTOMER", "AGENT", "MANAGER", "ADMIN"];

interface RoutePolicy {
  pattern: RegExp;
  roles: AppRole[];
}

const protectedRoutePolicies: RoutePolicy[] = [
  {
    pattern: /^\/tickets(?:\/.*)?$/,
    roles: ["CUSTOMER", "ADMIN"],
  },
  {
    pattern: /^\/agent(?:\/.*)?$/,
    roles: ["AGENT", "ADMIN"],
  },
  {
    pattern: /^\/reports(?:\/.*)?$/,
    roles: ["MANAGER", "ADMIN"],
  },
  {
    pattern: /^\/notifications(?:\/.*)?$/,
    roles: allRoles,
  },
];

export function getDefaultAuthenticatedPath(roles: AppRole[]) {
  if (roles.includes("AGENT") || roles.includes("ADMIN")) {
    return "/agent/inbox";
  }

  if (roles.includes("MANAGER")) {
    return "/reports";
  }

  return "/tickets";
}

export function getProtectedRouteRedirectPath(pathname: string, roles: AppRole[]) {
  if (roles.length === 0) {
    return undefined;
  }

  const policy = protectedRoutePolicies.find((routePolicy) =>
    routePolicy.pattern.test(pathname),
  );

  if (!policy || hasAnyRole(roles, policy.roles)) {
    return undefined;
  }

  return getDefaultAuthenticatedPath(roles);
}

function hasAnyRole(userRoles: AppRole[], allowedRoles: AppRole[]) {
  return userRoles.some((role) => allowedRoles.includes(role));
}
