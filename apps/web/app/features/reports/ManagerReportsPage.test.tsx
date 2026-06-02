import { ThemeProvider } from "@mui/material/styles";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen, waitFor } from "@testing-library/react";
import { HttpResponse, http } from "msw";
import { ManagerReportsPage } from "~/features/reports/ManagerReportsPage";
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
        <ManagerReportsPage />
      </ThemeProvider>
    </QueryClientProvider>,
  );
}

describe("ManagerReportsPage", () => {
  it("renders manager reports from REST APIs", async () => {
    server.use(
      http.get("*/api/reports/tickets/status-distribution", () =>
        HttpResponse.json({
          counts: [
            { count: 5, status: "NEW" },
            { count: 7, status: "IN_PROGRESS" },
          ],
          generatedAt: "2026-06-02T08:00:00Z",
          totalOpenTickets: 12,
        }),
      ),
      http.get("*/api/reports/tickets/closed", () =>
        HttpResponse.json({
          averageResolutionMinutes: 180,
          dailyCounts: [
            { count: 2, date: "2026-06-01" },
            { count: 3, date: "2026-06-02" },
          ],
          fromDate: "2026-05-04",
          generatedAt: "2026-06-02T08:01:00Z",
          priorityCounts: [
            { count: 4, priority: "HIGH" },
            { count: 1, priority: "MEDIUM" },
          ],
          toDate: "2026-06-02",
          totalClosedTickets: 5,
        }),
      ),
      http.get("*/api/reports/agents/performance", () =>
        HttpResponse.json({
          generatedAt: "2026-06-02T08:02:00Z",
          rows: [
            {
              agentId: "agent-1",
              assignedTicketCount: 8,
              averageResolutionMinutes: 150,
              resolvedTicketCount: 6,
              totalWorklogMinutes: 360,
            },
          ],
        }),
      ),
      http.get("*/api/reports/sla/compliance", () =>
        HttpResponse.json({
          activeTicketCount: 12,
          atRiskTicketCount: 2,
          breachedTicketCount: 1,
          compliancePercentage: 90.5,
          generatedAt: "2026-06-02T08:03:00Z",
          metTicketCount: 9,
          priorityBreakdown: [
            {
              activeTicketCount: 4,
              atRiskTicketCount: 1,
              breachedTicketCount: 0,
              compliancePercentage: 92.5,
              metTicketCount: 3,
              priority: "HIGH",
            },
          ],
        }),
      ),
    );

    renderPage();

    await waitFor(() => expect(screen.getByText("Yonetici raporu")).toBeInTheDocument());
    expect(screen.getByText("Kapanis hacmi")).toBeInTheDocument();
    expect(screen.getByText("Status dagilimi")).toBeInTheDocument();
    expect(screen.getByText("SLA dagilimi")).toBeInTheDocument();
    expect(screen.getByText("Agent performansi")).toBeInTheDocument();
    expect(screen.getAllByText("90.5%")).not.toHaveLength(0);
    expect(screen.getByText("agent-1")).toBeInTheDocument();
  });
});
