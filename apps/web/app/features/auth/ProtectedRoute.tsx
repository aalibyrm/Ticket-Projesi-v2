import type { ReactNode } from "react";
import { Navigate, useLocation } from "react-router";
import { AuthRequired } from "~/features/auth/components/AuthRequired";
import { RoleAwareShell } from "~/features/shell/RoleAwareShell";
import { LoadingScreen } from "~/shared/components/LoadingScreen";
import { useAppSelector } from "~/shared/store/hooks";
import {
  selectAuthError,
  selectAuthStatus,
  selectUserRoles,
} from "~/features/auth/authSlice";
import { getProtectedRouteRedirectPath } from "~/features/auth/roleRouting";

export function ProtectedRoute({ children }: { children: ReactNode }) {
  const location = useLocation();
  const status = useAppSelector(selectAuthStatus);
  const error = useAppSelector(selectAuthError);
  const roles = useAppSelector(selectUserRoles);

  if (status === "idle" || status === "loading") {
    return <LoadingScreen label="Uygulama yukleniyor" />;
  }

  if (status !== "authenticated") {
    return <AuthRequired error={error} />;
  }

  const redirectPath = getProtectedRouteRedirectPath(location.pathname, roles);
  if (redirectPath) {
    return <Navigate replace to={redirectPath} />;
  }

  return <RoleAwareShell>{children}</RoleAwareShell>;
}
