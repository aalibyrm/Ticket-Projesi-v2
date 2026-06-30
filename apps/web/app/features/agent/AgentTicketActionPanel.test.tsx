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
const leadId = "30000000-0000-0000-0000-000000000003";
const memberId = "40000000-0000-0000-0000-000000000003";
const ticketId = "22222222-2222-2222-2222-222222222222";

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

function ticketFixture(overrides: Partial<TicketResponse> = {}): TicketResponse {
  return {
    assigneeId: undefined,
    assignedTeamId: teamId,
    attachments: [],
    createdAt: "2026-05-30T08:00:00Z",
    customerId: "11111111-1111-1111-1111-111111111111",
    description: "Payment page is blank after card confirmation.",
    id: ticketId,
    priority: "HIGH",
    productCode: "PAY",
    productId: "33333333-3333-3333-3333-333333333333",
    productName: "Payment Gateway",
    status: "IN_PROGRESS",
    summary: "Odeme sayfasi yuklenmiyor",
    ticketNumber: "TCK-2026-0042",
    updatedAt: "2026-05-30T08:30:00Z",
    ...overrides,
  };
}

describe("AgentTicketActionPanel", () => {
  afterEach(() => {
    store.dispatch(setUnauthenticated());
  });

  it("allows a team lead to assign a ticket to a known team member", async () => {
    let assignmentBody: Record<string, unknown> | undefined;
    store.dispatch(setAuthenticated({
      displayName: "Seda Yildirim",
      id: leadId,
      roles: ["AGENT"],
      username: "lead.web@example.com",
    }));

    server.use(
      http.get(`*/api/v1/organization/teams/${teamId}/members`, () =>
        HttpResponse.json([
          {
            actorId: leadId,
            displayName: "Seda Yildirim",
            email: "seda.yildirim@example.local",
            teamCode: "WEB_APP_SUPPORT",
            teamId,
            teamLead: true,
          },
          {
            actorId: memberId,
            displayName: "Deniz Arslan",
            email: "deniz.arslan@example.local",
            teamCode: "WEB_APP_SUPPORT",
            teamId,
            teamLead: false,
          },
        ]),
      ),
      http.get("*/api/v1/agent/tickets/:ticketId/worklogs", () => HttpResponse.json([])),
      http.get("*/api/v1/agent/tickets/:ticketId/comments", () => HttpResponse.json([])),
      http.patch("*/api/v1/agent/tickets/:ticketId/assignment", async ({ request }) => {
        assignmentBody = await request.json() as Record<string, unknown>;
        return HttpResponse.json({
          ...ticket,
          assigneeId: memberId,
          assignedTeamId: teamId,
        });
      }),
    );

    const ticket = ticketFixture();

    renderPanel(ticket);

    expect(screen.queryByLabelText("Ekip")).not.toBeInTheDocument();
    await waitFor(() => expect(screen.getByLabelText("Agent")).toBeEnabled());
    expect(screen.getByText("Agent sec")).toBeInTheDocument();
    fireEvent.mouseDown(screen.getByLabelText("Agent"));
    fireEvent.click(await screen.findByRole("option", { name: /Deniz Arslan/ }));
    fireEvent.click(screen.getByRole("button", { name: "Atamayi kaydet" }));

    await waitFor(() =>
      expect(assignmentBody).toMatchObject({
        assigneeId: memberId,
        assignedTeamId: teamId,
      }),
    );
  });

  it("hides delegated assignment controls for a non-lead agent", async () => {
    let assignmentBody: Record<string, unknown> | undefined;
    let worklogRequestCount = 0;
    store.dispatch(setAuthenticated({
      displayName: "Deniz Arslan",
      id: memberId,
      roles: ["AGENT"],
      username: "agent.web@example.com",
    }));

    server.use(
      http.get(`*/api/v1/organization/teams/${teamId}/members`, () =>
        HttpResponse.json([
          {
            actorId: leadId,
            displayName: "Seda Yildirim",
            email: "seda.yildirim@example.local",
            teamCode: "WEB_APP_SUPPORT",
            teamId,
            teamLead: true,
          },
          {
            actorId: memberId,
            displayName: "Deniz Arslan",
            email: "deniz.arslan@example.local",
            teamCode: "WEB_APP_SUPPORT",
            teamId,
            teamLead: false,
          },
        ]),
      ),
      http.get("*/api/v1/agent/tickets/:ticketId/worklogs", () => {
        worklogRequestCount += 1;
        return HttpResponse.json([]);
      }),
      http.get("*/api/v1/agent/tickets/:ticketId/comments", () => HttpResponse.json([])),
      http.patch("*/api/v1/agent/tickets/:ticketId/assignment", async ({ request }) => {
        assignmentBody = await request.json() as Record<string, unknown>;
        return HttpResponse.json({
          ...ticket,
          assigneeId: memberId,
          assignedTeamId: teamId,
        });
      }),
    );

    const ticket = ticketFixture();

    renderPanel(ticket);

    expect(await screen.findByRole("button", { name: "Bana ata" })).toBeInTheDocument();
    expect(screen.queryByLabelText("Ekip")).not.toBeInTheDocument();
    expect(screen.queryByLabelText("Agent")).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Atamayi kaydet" })).not.toBeInTheDocument();
    expect(screen.queryByText("Worklog")).not.toBeInTheDocument();
    expect(worklogRequestCount).toBe(0);
    fireEvent.click(screen.getByRole("button", { name: "Bana ata" }));

    await waitFor(() =>
      expect(assignmentBody).toMatchObject({
        assigneeId: memberId,
        assignedTeamId: teamId,
      }),
    );
  });

  it("renders one status action per allowed transition for the assigned agent", async () => {
    store.dispatch(setAuthenticated({
      displayName: "Support Agent",
      id: memberId,
      roles: ["AGENT"],
      username: "agent@example.com",
    }));

    server.use(
      http.get("*/api/v1/agent/tickets/:ticketId/worklogs", () => HttpResponse.json([])),
      http.get("*/api/v1/agent/tickets/:ticketId/comments", () => HttpResponse.json([])),
    );

    renderPanel(ticketFixture({ assigneeId: memberId }));

    expect(await screen.findByText("Mevcut status")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Musteri bekleniyor yap" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Cozuldu yap" })).toBeInTheDocument();
    expect(screen.queryByLabelText("Sonraki status")).not.toBeInTheDocument();
  });

  it("hides status actions when the current agent is not assigned", async () => {
    let worklogRequestCount = 0;
    store.dispatch(setAuthenticated({
      displayName: "Support Agent",
      id: memberId,
      roles: ["AGENT"],
      username: "agent@example.com",
    }));

    server.use(
      http.get("*/api/v1/agent/tickets/:ticketId/worklogs", () => {
        worklogRequestCount += 1;
        return HttpResponse.json([]);
      }),
      http.get("*/api/v1/agent/tickets/:ticketId/comments", () => HttpResponse.json([])),
    );

    renderPanel(ticketFixture({ assigneeId: "50000000-0000-0000-0000-000000000003" }));

    expect(await screen.findByText("Dosyalar")).toBeInTheDocument();
    expect(screen.queryByText("Mevcut status")).not.toBeInTheDocument();
    expect(screen.queryByText("Atama")).not.toBeInTheDocument();
    expect(screen.queryByText("Worklog")).not.toBeInTheDocument();
    expect(worklogRequestCount).toBe(0);
    expect(screen.queryByRole("button", { name: "Musteri bekleniyor yap" })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Cozuldu yap" })).not.toBeInTheDocument();
  });

  it("hides worklog controls for an unassigned ticket", async () => {
    let worklogRequestCount = 0;
    store.dispatch(setAuthenticated({
      displayName: "Support Agent",
      id: memberId,
      roles: ["AGENT"],
      username: "agent@example.com",
    }));

    server.use(
      http.get("*/api/v1/agent/tickets/:ticketId/worklogs", () => {
        worklogRequestCount += 1;
        return HttpResponse.json([]);
      }),
      http.get("*/api/v1/agent/tickets/:ticketId/comments", () => HttpResponse.json([])),
    );

    renderPanel(ticketFixture());

    expect(await screen.findByText("Atama")).toBeInTheDocument();
    expect(screen.queryByText("Worklog")).not.toBeInTheDocument();
    expect(screen.queryByLabelText("Tarih")).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Worklog ekle" })).not.toBeInTheDocument();
    expect(worklogRequestCount).toBe(0);
  });

  it("hides assignment controls when the ticket already has an assignee", async () => {
    store.dispatch(setAuthenticated({
      displayName: "Support Agent",
      id: memberId,
      roles: ["AGENT"],
      username: "agent@example.com",
    }));

    server.use(
      http.get("*/api/v1/agent/tickets/:ticketId/worklogs", () => HttpResponse.json([])),
      http.get("*/api/v1/agent/tickets/:ticketId/comments", () => HttpResponse.json([])),
    );

    renderPanel(ticketFixture({ assigneeId: memberId }));

    expect(await screen.findByText("Worklog")).toBeInTheDocument();
    expect(screen.queryByText("Atama")).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Bana ata" })).not.toBeInTheDocument();
    expect(screen.queryByLabelText("Ekip")).not.toBeInTheDocument();
    expect(screen.queryByLabelText("Agent")).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Atamayi kaydet" })).not.toBeInTheDocument();
  });

  it("does not show resume action while waiting for customer without a customer reply", async () => {
    store.dispatch(setAuthenticated({
      displayName: "Support Agent",
      id: memberId,
      roles: ["AGENT"],
      username: "agent@example.com",
    }));

    server.use(
      http.get("*/api/v1/agent/tickets/:ticketId/worklogs", () => HttpResponse.json([])),
      http.get("*/api/v1/agent/tickets/:ticketId/comments", () => HttpResponse.json([])),
    );

    renderPanel(ticketFixture({ assigneeId: memberId, status: "WAITING_FOR_CUSTOMER" }));

    expect(await screen.findByText("Uygun statu gecisi yok.")).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Islemde yap" })).not.toBeInTheDocument();
  });

  it("shows resume action after a customer external reply", async () => {
    store.dispatch(setAuthenticated({
      displayName: "Support Agent",
      id: memberId,
      roles: ["AGENT"],
      username: "agent@example.com",
    }));

    server.use(
      http.get("*/api/v1/agent/tickets/:ticketId/worklogs", () => HttpResponse.json([])),
      http.get("*/api/v1/agent/tickets/:ticketId/comments", () =>
        HttpResponse.json([
          {
            authorId: "11111111-1111-1111-1111-111111111111",
            body: "I sent the requested information.",
            createdAt: "2026-05-30T09:00:00Z",
            id: "44444444-4444-4444-4444-444444444444",
            ticketId,
            visibility: "EXTERNAL",
          },
        ]),
      ),
    );

    renderPanel(ticketFixture({ assigneeId: memberId, status: "WAITING_FOR_CUSTOMER" }));

    expect(await screen.findByRole("button", { name: "Islemde yap" })).toBeInTheDocument();
  });
});
