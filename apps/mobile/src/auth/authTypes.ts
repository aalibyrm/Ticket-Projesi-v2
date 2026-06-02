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
  signIn: () => Promise<void>;
  signOut: () => Promise<void>;
}
