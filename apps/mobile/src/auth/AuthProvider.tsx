import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import * as AuthSession from "expo-auth-session";
import * as WebBrowser from "expo-web-browser";
import { setAccessTokenProvider } from "../api/httpClient";
import { getMissingAuthConfig, isAuthConfigured, mobileEnv } from "../config/env";
import { clearStoredTokens, loadStoredTokens, storeTokens } from "./secureTokenStorage";
import type { AuthContextValue, AuthStatus, AuthTokens } from "./authTypes";
import { isTokenUsable } from "./tokenState";

WebBrowser.maybeCompleteAuthSession();

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [tokens, setTokens] = useState<AuthTokens | undefined>();
  const [status, setStatus] = useState<AuthStatus>("loading");
  const [error, setError] = useState<string | undefined>();

  const discovery = AuthSession.useAutoDiscovery(mobileEnv.keycloakIssuerUrl);
  const redirectUri = AuthSession.makeRedirectUri({
    scheme: mobileEnv.authRedirectScheme,
    path: "auth/callback"
  });
  const [request, response, promptAsync] = AuthSession.useAuthRequest(
    {
      clientId: mobileEnv.keycloakClientId,
      redirectUri,
      responseType: AuthSession.ResponseType.Code,
      scopes: ["openid", "profile", "email"],
      usePKCE: true
    },
    discovery
  );

  useEffect(() => {
    let mounted = true;

    async function restoreSession() {
      const storedTokens = await loadStoredTokens();

      if (!mounted) {
        return;
      }

      if (isTokenUsable(storedTokens)) {
        setTokens(storedTokens);
        setStatus("authenticated");
      } else {
        await clearStoredTokens();
        setStatus("unauthenticated");
      }
    }

    void restoreSession();

    return () => {
      mounted = false;
    };
  }, []);

  useEffect(() => {
    setAccessTokenProvider(() => tokens?.accessToken);
  }, [tokens]);

  useEffect(() => {
    let cancelled = false;

    async function exchangeCode(code: string) {
      if (!discovery || !request?.codeVerifier) {
        return;
      }

      setStatus("loading");
      setError(undefined);

      try {
        const tokenResponse = await AuthSession.exchangeCodeAsync(
          {
            clientId: mobileEnv.keycloakClientId,
            code,
            redirectUri,
            extraParams: {
              code_verifier: request.codeVerifier
            }
          },
          discovery
        );

        if (cancelled) {
          return;
        }

        const nextTokens: AuthTokens = {
          accessToken: tokenResponse.accessToken,
          refreshToken: tokenResponse.refreshToken,
          idToken: tokenResponse.idToken,
          tokenType: tokenResponse.tokenType,
          issuedAt: tokenResponse.issuedAt,
          expiresAt: tokenResponse.expiresIn
            ? Date.now() + tokenResponse.expiresIn * 1000
            : undefined
        };

        await storeTokens(nextTokens);
        setTokens(nextTokens);
        setStatus("authenticated");
      } catch {
        if (!cancelled) {
          setStatus("error");
          setError("Oturum acma islemi tamamlanamadi.");
        }
      }
    }

    if (response?.type === "success" && response.params.code) {
      void exchangeCode(response.params.code);
    }

    if (response?.type === "error") {
      setStatus("error");
      setError("Kimlik saglayici oturum acma istegini reddetti.");
    }

    return () => {
      cancelled = true;
    };
  }, [discovery, redirectUri, request?.codeVerifier, response]);

  const value = useMemo<AuthContextValue>(
    () => ({
      status,
      accessToken: tokens?.accessToken,
      error,
      signIn: async () => {
        if (!isAuthConfigured()) {
          const missingKeys = getMissingAuthConfig().join(", ");
          setStatus("error");
          setError(`Eksik mobil auth konfigurasyonu: ${missingKeys}`);
          return;
        }

        if (!request) {
          setStatus("error");
          setError("Kimlik saglayici henuz hazir degil.");
          return;
        }

        setError(undefined);
        await promptAsync();
      },
      signOut: async () => {
        await clearStoredTokens();
        setTokens(undefined);
        setStatus("unauthenticated");
      }
    }),
    [error, promptAsync, request, status, tokens?.accessToken]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }

  return context;
}
