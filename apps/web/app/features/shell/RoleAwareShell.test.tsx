import { configureStore } from "@reduxjs/toolkit";
import { ThemeProvider } from "@mui/material/styles";
import { render, screen } from "@testing-library/react";
import { Provider } from "react-redux";
import { MemoryRouter } from "react-router";
import type { AppRole } from "~/features/auth/authTypes";
import type { AuthState } from "~/features/auth/authSlice";
import authReducer from "~/features/auth/authSlice";
import { RoleAwareShell } from "~/features/shell/RoleAwareShell";
import { appTheme } from "~/shared/theme/appTheme";

function renderShell(roles: AppRole[]) {
  const authState = {
    status: "authenticated",
    user: {
      displayName: "Manager User",
      id: "00000000-0000-0000-0000-000000000001",
      roles,
      username: "manager.user",
    },
  } satisfies AuthState;

  const store = configureStore({
    reducer: {
      auth: authReducer,
    },
    preloadedState: {
      auth: authState,
    },
  });

  return render(
    <Provider store={store}>
      <ThemeProvider theme={appTheme}>
        <MemoryRouter>
          <RoleAwareShell>
            <main>Panel content</main>
          </RoleAwareShell>
        </MemoryRouter>
      </ThemeProvider>
    </Provider>,
  );
}

describe("RoleAwareShell", () => {
  it("shows manager navigation without customer-only ticket actions", () => {
    renderShell(["MANAGER"]);

    expect(screen.getByLabelText("Raporlar")).toBeInTheDocument();
    expect(screen.queryByLabelText("Taleplerim")).not.toBeInTheDocument();
    expect(screen.queryByLabelText("Yeni talep")).not.toBeInTheDocument();
    expect(screen.getByText("SupportHub")).toBeInTheDocument();
    expect(screen.getByLabelText("Kullanici: Manager User")).toBeInTheDocument();
  });

  it("shows agent navigation without customer ticket list action", () => {
    renderShell(["AGENT"]);

    expect(screen.getByLabelText("Temsilci paneli")).toBeInTheDocument();
    expect(screen.queryByLabelText("Taleplerim")).not.toBeInTheDocument();
    expect(screen.queryByLabelText("Yeni talep")).not.toBeInTheDocument();
  });
});
