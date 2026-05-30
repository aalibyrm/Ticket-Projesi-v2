import { Outlet } from "react-router";
import { ProtectedRoute } from "~/features/auth/ProtectedRoute";

export default function ProtectedLayoutRoute() {
  return (
    <ProtectedRoute>
      <Outlet />
    </ProtectedRoute>
  );
}
