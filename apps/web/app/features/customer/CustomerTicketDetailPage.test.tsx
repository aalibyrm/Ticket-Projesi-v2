import { ThemeProvider } from "@mui/material/styles";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { HttpResponse, http } from "msw";
import { MemoryRouter, Route, Routes } from "react-router";
import { CustomerTicketDetailPage } from "~/features/customer/CustomerTicketDetailPage";
import { appTheme } from "~/shared/theme/appTheme";
import { server } from "~/test/msw/server";

function renderPage(ticketId: string) {
  const queryClient = new QueryClient({
    defaultOptions: {
      mutations: {
        retry: false,
      },
      queries: {
        retry: false,
      },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={appTheme}>
        <MemoryRouter initialEntries={[`/tickets/${ticketId}`]}>
          <Routes>
            <Route element={<CustomerTicketDetailPage />} path="/tickets/:ticketId" />
          </Routes>
        </MemoryRouter>
      </ThemeProvider>
    </QueryClientProvider>,
  );
}

describe("CustomerTicketDetailPage", () => {
  it("opens assigned agent summary drawer for the customer ticket", async () => {
    const ticketId = "22222222-2222-2222-2222-222222222222";

    server.use(
      http.get("*/api/v1/tickets/:ticketId", () =>
        HttpResponse.json({
          assigneeId: "40000000-0000-0000-0000-000000000008",
          assignedTeamId: "20000000-0000-0000-0000-000000000008",
          attachments: [],
          createdAt: "2026-05-30T08:00:00Z",
          customerId: "80000000-0000-0000-0000-000000000001",
          description: "Odeme akisi kart dogrulamasindan sonra hata veriyor.",
          id: ticketId,
          priority: "HIGH",
          productCode: "MOBILE",
          productId: "33333333-3333-3333-3333-333333333333",
          productName: "Mobile App",
          status: "IN_PROGRESS",
          summary: "Odeme olmuyor",
          ticketNumber: "TCK-2026-0044",
          updatedAt: "2026-05-30T08:30:00Z",
        }),
      ),
      http.get("*/api/v1/tickets/:ticketId/agent-summary", () =>
        HttpResponse.json({
          agentId: "40000000-0000-0000-0000-000000000008",
          assigned: true,
          assignedTeamId: "20000000-0000-0000-0000-000000000008",
          displayName: "Payment Agent",
          email: "agent.payment@example.local",
          resolvedTicketCount: 14,
          slaBreachedTicketCount: 2,
          slaCompliancePercentage: 87.5,
          slaMetTicketCount: 14,
        }),
      ),
      http.get("*/api/v1/tickets/:ticketId/comments", () => HttpResponse.json([])),
      http.get("*/api/v1/tickets/:ticketId/comments/read-state", () =>
        HttpResponse.json({
          lastReadAt: null,
          ticketId,
          unreadCount: 0,
        }),
      ),
    );

    renderPage(ticketId);

    await waitFor(() => expect(screen.getByText("Odeme olmuyor")).toBeInTheDocument());
    const agentButton = await screen.findByRole("button", { name: /Temsilci: Payment Agent/i });
    fireEvent.click(agentButton);

    expect(await screen.findByText("Temsilci detayi")).toBeInTheDocument();
    expect(screen.getByText("Payment Agent")).toBeInTheDocument();
    expect(screen.getByText("agent.payment@example.local")).toBeInTheDocument();
    expect(screen.getByText("SLA uyumu")).toBeInTheDocument();
    expect(screen.getByText("87.5%")).toBeInTheDocument();
    expect(screen.getByText("Cozdugu ticket")).toBeInTheDocument();
    expect(screen.getAllByText("14").length).toBeGreaterThan(0);
  });
});
