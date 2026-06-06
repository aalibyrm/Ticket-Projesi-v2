import { ThemeProvider } from "@mui/material/styles";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { Provider as ReduxProvider } from "react-redux";
import { HttpResponse, http } from "msw";
import { afterEach } from "vitest";
import { AgentTicketActionPanel } from "~/features/agent/components/AgentTicketActionPanel";
import type { TicketResponse } from "~/features/agent/agentTypes";
import { setAuthenticated, setUnauthenticated } from "~/features/auth/authSlice";
import { store } from "~/shared/store/store";
import { appTheme } from "~/shared/theme/appTheme";
import { server } from "~/test/msw/server";

const teamId = "20000000-0000-0000-0000-000000000003";
const memberId = "40000000-0000-0000-0000-000000000003";

function renderPanel(ticket: TicketResponse) {
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
    <ReduxProvider store={store}>
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={appTheme}>
          <AgentTicketActionPanel ticket={ticket} />
        </ThemeProvider>
      </QueryClientProvider>
    </ReduxProvider>,
  );
}

describe("AgentTicketActionPanel", () => {
  afterEach(() => {
    store.dispatch(setUnauthenticated());
  });

  it("assigns ticket by selecting a known team member", async () => {
    let assignmentBody: Record<string, unknown> | undefined;
    store.dispatch(setAuthenticated({
      displayName: "Support Agent",
      id: memberId,
      roles: ["AGENT"],
      username: "agent@example.com",
    }));

    server.use(
      http.get("*/api/v1/organization/teams", () =>
        HttpResponse.json([
          {
            code: "WEB_APP_SUPPORT",
            departmentCode: "APPLICATION_SUPPORT",
            departmentId: "10000000-0000-0000-0000-000000000002",
            id: teamId,
            leadActorId: "30000000-0000-0000-0000-000000000003",
            name: "Web App Support",
          },
        ]),
      ),
      http.get(`*/api/v1/organization/teams/${teamId}/members`, () =>
        HttpResponse.json([
          {
            actorId: "30000000-0000-0000-0000-000000000003",
            displayName: "Web Lead",
            email: "lead.web@example.local",
            teamCode: "WEB_APP_SUPPORT",
            teamId,
            teamLead: true,
          },
          {
            actorId: memberId,
            displayName: "Web Agent",
            email: "agent.web@example.local",
            teamCode: "WEB_APP_SUPPORT",
            teamId,
            teamLead: false,
          },
        ]),
      ),
      http.get("*/api/v1/agent/tickets/:ticketId/worklogs", () => HttpResponse.json([])),
      http.patch("*/api/v1/agent/tickets/:ticketId/assignment", async ({ request }) => {
        assignmentBody = await request.json() as Record<string, unknown>;
        return HttpResponse.json({
          ...ticket,
          assigneeId: memberId,
          assignedTeamId: teamId,
        });
      }),
    );

    const ticket: TicketResponse = {
      assigneeId: undefined,
      assignedTeamId: teamId,
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
    };

    renderPanel(ticket);

    await waitFor(() => expect(screen.getByLabelText("Ekip")).toBeEnabled());
    await waitFor(() => expect(screen.getByLabelText("Agent")).toBeEnabled());
    expect(screen.getByText("Web Agent")).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText("Agent"), {
      target: { value: memberId },
    });
    fireEvent.click(screen.getByRole("button", { name: "Atamayi kaydet" }));

    await waitFor(() =>
      expect(assignmentBody).toMatchObject({
        assigneeId: memberId,
        assignedTeamId: teamId,
      }),
    );
  });
});
