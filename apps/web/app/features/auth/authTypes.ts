export type AppRole = "CUSTOMER" | "AGENT" | "MANAGER" | "ADMIN";

export interface AuthUser {
  displayName: string;
  roles: AppRole[];
  username: string;
}

export type AuthStatus = "idle" | "loading" | "authenticated" | "unauthenticated" | "error";
