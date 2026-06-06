import { ThemeProvider } from "@mui/material/styles";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen, waitFor } from "@testing-library/react";
import { HttpResponse, http } from "msw";
import { MemoryRouter } from "react-router";
import { CustomerTicketListPage } from "~/features/customer/CustomerTicketListPage";
import { appTheme } from "~/shared/theme/appTheme";
import { server } from "~/test/msw/server";

function renderPage() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={appTheme}>
        <MemoryRouter>
          <CustomerTicketListPage />
        </MemoryRouter>
      </ThemeProvider>
    </QueryClientProvider>,
  );
}

describe("CustomerTicketListPage", () => {
  it("renders customer tickets from REST API", async () => {
    server.use(
      http.get("*/api/v1/tickets", () =>
        HttpResponse.json([
          {
            assigneeId: null,
            assignedTeamId: null,
            attachments: [],
            createdAt: "2026-05-30T08:00:00Z",
            customerId: "11111111-1111-1111-1111-111111111111",
            description: "Dashboard error after login.",
            id: "22222222-2222-2222-2222-222222222222",
            priority: "HIGH",
            productCode: "CORE",
            productId: "33333333-3333-3333-3333-333333333333",
            productName: "Core Platform",
            status: "NEW",
            summary: "Dashboard acilmiyor",
            ticketNumber: "TCK-2026-0001",
            updatedAt: "2026-05-30T08:30:00Z",
          },
        ]),
      ),
    );

    renderPage();

    await waitFor(() => expect(screen.getByText("Dashboard acilmiyor")).toBeInTheDocument());
    expect(screen.getByText("TCK-2026-0001")).toBeInTheDocument();
    expect(screen.getByRole("row", { name: "TCK-2026-0001 ticket detayini ac" })).toBeInTheDocument();
  });
});
