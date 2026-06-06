import { configureStore } from "@reduxjs/toolkit";
import { ThemeProvider } from "@mui/material/styles";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { HttpResponse, http } from "msw";
import { Provider } from "react-redux";
import { MemoryRouter, useLocation } from "react-router";
import type { AppRole } from "~/features/auth/authTypes";
import type { AuthState } from "~/features/auth/authSlice";
import authReducer from "~/features/auth/authSlice";
import { CustomerNotificationsPage } from "~/features/customer/CustomerNotificationsPage";
import type { NotificationResponse } from "~/features/customer/customerTypes";
import { appTheme } from "~/shared/theme/appTheme";
import { server } from "~/test/msw/server";

const ticketId = "6c11f312-4b4c-460f-a3ee-b16017367d29";

function renderNotificationsPage(roles: AppRole[]) {
  const notification = {
    createdAt: "2026-06-06T14:30:00Z",
    id: "notification-1",
    message: "Ticket TCK-001004 was created and routed to support.",
    read: false,
    ticketId,
    title: "New support ticket",
    type: "TICKET_CREATED",
  } satisfies NotificationResponse;

  server.use(
    http.get("*/api/v1/notifications", () => HttpResponse.json([notification])),
    http.patch("*/api/v1/notifications/:notificationId/read", () =>
      HttpResponse.json({ ...notification, read: true })),
  );

  const authState = {
    status: "authenticated",
    user: {
      displayName: "Demo User",
      id: "00000000-0000-0000-0000-000000000001",
      roles,
      username: "demo.user",
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
          <MemoryRouter initialEntries={["/notifications"]}>
            <CustomerNotificationsPage />
            <LocationProbe />
          </MemoryRouter>
        </ThemeProvider>
      </QueryClientProvider>
    </Provider>,
  );
}

describe("CustomerNotificationsPage", () => {
  it("navigates support users to the agent ticket detail when a notification is clicked", async () => {
    renderNotificationsPage(["AGENT"]);

    fireEvent.click(await screen.findByText("New support ticket"));

    await waitFor(() => {
      expect(screen.getByTestId("location")).toHaveTextContent(`/agent/tickets/${ticketId}`);
    });
  });

  it("navigates customers to the customer ticket detail when a notification is clicked", async () => {
    renderNotificationsPage(["CUSTOMER"]);

    fireEvent.click(await screen.findByText("New support ticket"));

    await waitFor(() => {
      expect(screen.getByTestId("location")).toHaveTextContent(`/tickets/${ticketId}`);
    });
  });
});

function LocationProbe() {
  const location = useLocation();

  return <span data-testid="location">{location.pathname}</span>;
}
