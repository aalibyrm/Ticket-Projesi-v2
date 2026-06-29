import { configureStore } from "@reduxjs/toolkit";
import { ThemeProvider } from "@mui/material/styles";
import { render, screen } from "@testing-library/react";
import { Provider } from "react-redux";
import { MemoryRouter } from "react-router";
import authReducer, { type AuthState } from "~/features/auth/authSlice";
import { ProtectedRoute } from "~/features/auth/ProtectedRoute";
import { appTheme } from "~/shared/theme/appTheme";

function renderProtectedRoute(authState: AuthState) {
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
        <MemoryRouter initialEntries={["/tickets"]}>
          <ProtectedRoute>
            <main>Protected content</main>
          </ProtectedRoute>
        </MemoryRouter>
      </ThemeProvider>
    </Provider>,
  );
}

describe("ProtectedRoute", () => {
  it("shows a controlled error for authenticated users without app roles", () => {
    renderProtectedRoute({
      status: "authenticated",
      user: {
        displayName: "Roleless User",
        id: "roleless-user",
        roles: [],
        username: "roleless.user",
      },
    });

    expect(screen.getByRole("alert")).toHaveTextContent(
      "Hesabiniza uygulama rolu tanimli degil.",
    );
    expect(screen.queryByText("Protected content")).not.toBeInTheDocument();
  });
});
