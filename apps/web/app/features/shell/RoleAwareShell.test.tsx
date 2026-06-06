import { configureStore } from "@reduxjs/toolkit";
import { ThemeProvider } from "@mui/material/styles";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import { HttpResponse, http } from "msw";
import { Provider } from "react-redux";
import { MemoryRouter } from "react-router";
import type { AppRole } from "~/features/auth/authTypes";
import type { AuthState } from "~/features/auth/authSlice";
import type { NotificationResponse } from "~/features/customer/customerTypes";
import authReducer from "~/features/auth/authSlice";
import { RoleAwareShell } from "~/features/shell/RoleAwareShell";
import { appTheme } from "~/shared/theme/appTheme";
import { server } from "~/test/msw/server";

function renderShell(roles: AppRole[], unreadNotifications: NotificationResponse[] = []) {
  server.use(
    http.get("*/api/notifications", ({ request }) => {
      const url = new URL(request.url);
      if (url.searchParams.get("read") === "false") {
        return HttpResponse.json(unreadNotifications);
      }
      return HttpResponse.json([]);
    }),
  );

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
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

  return render(
    <Provider store={store}>
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={appTheme}>
          <MemoryRouter>
            <RoleAwareShell>
              <main>Panel content</main>
            </RoleAwareShell>
          </MemoryRouter>
        </ThemeProvider>
      </QueryClientProvider>
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

  it("shows unread notification count on the top bar bell", async () => {
    renderShell(["AGENT"], [
      {
        createdAt: "2026-06-06T14:30:00Z",
        id: "notification-1",
        message: "Ticket TCK-001004 has a new message.",
        read: false,
        title: "New ticket message",
        type: "TICKET_EXTERNAL_COMMENT_ADDED",
      },
      {
        createdAt: "2026-06-06T14:31:00Z",
        id: "notification-2",
        message: "Ticket TCK-001005 has a new message.",
        read: false,
        title: "New ticket message",
        type: "TICKET_EXTERNAL_COMMENT_ADDED",
      },
    ]);

    expect(await screen.findByLabelText("Bildirimler, 2 okunmamis")).toBeInTheDocument();
    expect(screen.getByText("2")).toBeInTheDocument();
  });
});
