import { describe, expect, it } from "vitest";
import {
  getDefaultAuthenticatedPath,
  getProtectedRouteRedirectPath,
} from "~/features/auth/roleRouting";

describe("roleRouting", () => {
  it("resolves the default landing page from the strongest available role", () => {
    expect(getDefaultAuthenticatedPath(["AGENT"])).toBe("/agent/inbox");
    expect(getDefaultAuthenticatedPath(["MANAGER"])).toBe("/reports");
    expect(getDefaultAuthenticatedPath(["CUSTOMER"])).toBe("/tickets");
    expect(getDefaultAuthenticatedPath(["ADMIN"])).toBe("/agent/inbox");
  });

  it("redirects authenticated users away from routes their role should not use", () => {
    expect(getProtectedRouteRedirectPath("/tickets", ["AGENT"])).toBe("/agent/inbox");
    expect(getProtectedRouteRedirectPath("/tickets/123", ["MANAGER"])).toBe("/reports");
    expect(getProtectedRouteRedirectPath("/reports", ["CUSTOMER"])).toBe("/tickets");
  });

  it("keeps users on routes allowed by their role", () => {
    expect(getProtectedRouteRedirectPath("/agent/inbox", ["AGENT"])).toBeUndefined();
    expect(getProtectedRouteRedirectPath("/reports", ["MANAGER"])).toBeUndefined();
    expect(getProtectedRouteRedirectPath("/tickets", ["CUSTOMER"])).toBeUndefined();
    expect(getProtectedRouteRedirectPath("/tickets/123", ["ADMIN"])).toBeUndefined();
  });
});
