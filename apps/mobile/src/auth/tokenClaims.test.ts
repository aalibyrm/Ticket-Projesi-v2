import { extractRoles, getAuthUserFromAccessToken } from "./tokenClaims";

function createUnsignedToken(payload: object) {
  const encodedPayload = Buffer.from(JSON.stringify(payload), "utf8")
    .toString("base64url");

  return `header.${encodedPayload}.signature`;
}

describe("token claims", () => {
  it("extracts normalized realm roles", () => {
    expect(extractRoles({ realm_access: { roles: ["role_customer", "manager", "unknown"] } })).toEqual([
      "CUSTOMER",
      "MANAGER"
    ]);
  });

  it("maps access token claims to an auth user", () => {
    const token = createUnsignedToken({
      name: "Agent User",
      preferred_username: "agent.user",
      realm_access: { roles: ["AGENT"] },
      sub: "agent-id"
    });

    expect(getAuthUserFromAccessToken(token)).toEqual({
      displayName: "Agent User",
      id: "agent-id",
      roles: ["AGENT"],
      username: "agent.user"
    });
  });

  it("rejects malformed tokens", () => {
    expect(getAuthUserFromAccessToken("bad-token")).toBeUndefined();
  });
});
