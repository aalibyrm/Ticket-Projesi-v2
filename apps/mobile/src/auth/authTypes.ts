export type AppRole = "CUSTOMER" | "AGENT" | "MANAGER" | "ADMIN";

export interface AuthUser {
  displayName: string;
  id: string;
  roles: AppRole[];
  username: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken?: string;
  idToken?: string;
  tokenType?: string;
  issuedAt?: number;
  expiresAt?: number;
}

export type AuthStatus = "loading" | "authenticated" | "unauthenticated" | "error";

export interface AuthContextValue {
  status: AuthStatus;
  accessToken?: string;
  error?: string;
  user?: AuthUser;
  signIn: () => Promise<void>;
  signOut: () => Promise<void>;
}
