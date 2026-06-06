import { Navigate } from "react-router";
import { AuthRequired } from "~/features/auth/components/AuthRequired";
import {
  selectAuthError,
  selectAuthStatus,
  selectUserRoles,
} from "~/features/auth/authSlice";
import { getDefaultAuthenticatedPath } from "~/features/auth/roleRouting";
import { LoadingScreen } from "~/shared/components/LoadingScreen";
import { useAppSelector } from "~/shared/store/hooks";

export default function HomeRoute() {
  const status = useAppSelector(selectAuthStatus);
  const error = useAppSelector(selectAuthError);
  const roles = useAppSelector(selectUserRoles);

  if (status === "idle" || status === "loading") {
    return <LoadingScreen label="Uygulama yukleniyor" />;
  }

  if (status !== "authenticated") {
    return <AuthRequired error={error} />;
  }

  return <Navigate replace to={getDefaultAuthenticatedPath(roles)} />;
}
