import { isTokenUsable } from "./tokenState";

describe("token state", () => {
  it("rejects missing tokens", () => {
    expect(isTokenUsable(undefined, 1000)).toBe(false);
  });

  it("accepts non-expiring access tokens", () => {
    expect(isTokenUsable({ accessToken: "token" }, 1000)).toBe(true);
  });

  it("rejects tokens inside expiry skew", () => {
    expect(isTokenUsable({ accessToken: "token", expiresAt: 20000 }, 1000)).toBe(false);
  });

  it("accepts tokens outside expiry skew", () => {
    expect(isTokenUsable({ accessToken: "token", expiresAt: 40000 }, 1000)).toBe(true);
  });
});
