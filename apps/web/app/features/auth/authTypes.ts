export type AppRole = "CUSTOMER" | "AGENT" | "MANAGER" | "ADMIN";

export interface AuthUser {
  displayName: string;
  id: string;
  roles: AppRole[];
  username: string;
}

export type AuthStatus = "idle" | "loading" | "authenticated" | "unauthenticated" | "error";
