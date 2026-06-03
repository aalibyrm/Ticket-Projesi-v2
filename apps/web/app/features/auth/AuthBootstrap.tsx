import { useEffect, useRef } from "react";
import {
  getAccessToken,
  initializeAuth,
} from "~/features/auth/authService";
import {
  setAuthenticated,
  setAuthError,
  setAuthLoading,
  setUnauthenticated,
} from "~/features/auth/authSlice";
import { setAccessTokenProvider } from "~/shared/api/httpClient";
import { useAppDispatch } from "~/shared/store/hooks";

export function AuthBootstrap() {
  const dispatch = useAppDispatch();
  const initializationRef = useRef<ReturnType<typeof initializeAuth>>();

  useEffect(() => {
    let cancelled = false;

    dispatch(setAuthLoading());
    initializationRef.current ??= initializeAuth();
    initializationRef.current
      .then((user) => {
        if (cancelled) {
          return;
        }

        if (user) {
          setAccessTokenProvider(getAccessToken);
          dispatch(setAuthenticated(user));
        } else {
          setAccessTokenProvider(undefined);
          dispatch(setUnauthenticated());
        }
      })
      .catch(() => {
        if (!cancelled) {
          setAccessTokenProvider(undefined);
          dispatch(setAuthError("Keycloak baglantisi dogrulanamadi."));
        }
      });

    return () => {
      cancelled = true;
      setAccessTokenProvider(undefined);
    };
  }, [dispatch]);

  return null;
}
