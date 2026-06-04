import type { ReactNode } from "react";
import { AuthRequired } from "~/features/auth/components/AuthRequired";
import { RoleAwareShell } from "~/features/shell/RoleAwareShell";
import { LoadingScreen } from "~/shared/components/LoadingScreen";
import { useAppSelector } from "~/shared/store/hooks";
import {
  selectAuthError,
  selectAuthStatus,
} from "~/features/auth/authSlice";

export function ProtectedRoute({ children }: { children: ReactNode }) {
  const status = useAppSelector(selectAuthStatus);
  const error = useAppSelector(selectAuthError);

  if (status === "idle" || status === "loading") {
    return <LoadingScreen label="Uygulama yukleniyor" />;
  }

  if (status !== "authenticated") {
    return <AuthRequired error={error} />;
  }

  return <RoleAwareShell>{children}</RoleAwareShell>;
}
