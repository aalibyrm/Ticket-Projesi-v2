import { ThemeProvider } from "@mui/material/styles";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen, waitFor } from "@testing-library/react";
import { HttpResponse, http } from "msw";
import { MemoryRouter } from "react-router";
import { AgentWorkbenchPage } from "~/features/agent/AgentWorkbenchPage";
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
          <AgentWorkbenchPage />
        </MemoryRouter>
      </ThemeProvider>
    </QueryClientProvider>,
  );
}

describe("AgentWorkbenchPage", () => {
  it("renders assigned ticket queue from REST API", async () => {
    server.use(
      http.get("*/api/v1/agent/tickets", () =>
        HttpResponse.json([
          {
            assigneeId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
            assignedTeamId: "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
            attachments: [],
            createdAt: "2026-05-30T08:00:00Z",
            customerId: "11111111-1111-1111-1111-111111111111",
            description: "Payment page is blank after card confirmation.",
            id: "22222222-2222-2222-2222-222222222222",
            priority: "HIGH",
            productCode: "PAY",
            productId: "33333333-3333-3333-3333-333333333333",
            productName: "Payment Gateway",
            status: "IN_PROGRESS",
            summary: "Odeme sayfasi yuklenmiyor",
            ticketNumber: "TCK-2026-0042",
            updatedAt: "2026-05-30T08:30:00Z",
          },
        ]),
      ),
    );

    renderPage();

    await waitFor(() => expect(screen.getAllByText("Odeme sayfasi yuklenmiyor")).not.toHaveLength(0));
    expect(screen.getByText("TCK-2026-0042")).toBeInTheDocument();
    expect(screen.getAllByText("Yuksek")).not.toHaveLength(0);
  });
});
