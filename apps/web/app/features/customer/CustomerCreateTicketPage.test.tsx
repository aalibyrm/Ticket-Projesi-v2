import { ThemeProvider } from "@mui/material/styles";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { HttpResponse, http } from "msw";
import { MemoryRouter } from "react-router";
import { CustomerCreateTicketPage } from "~/features/customer/CustomerCreateTicketPage";
import { appTheme } from "~/shared/theme/appTheme";
import { server } from "~/test/msw/server";

function renderPage() {
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
        <MemoryRouter>
          <CustomerCreateTicketPage />
        </MemoryRouter>
      </ThemeProvider>
    </QueryClientProvider>,
  );
}

describe("CustomerCreateTicketPage", () => {
  it("creates ticket with selected topic code", async () => {
    const productId = "33333333-3333-3333-3333-333333333333";
    let requestBody: Record<string, unknown> | undefined;

    server.use(
      http.get("*/api/products", () =>
        HttpResponse.json([
          {
            code: "CORE",
            id: productId,
            name: "Core Platform",
          },
        ]),
      ),
      http.get("*/api/ticket-topics", () =>
        HttpResponse.json([
          {
            code: "WEB_PORTAL_BUG",
            description: "Web portal defects and customer-facing web application errors.",
            id: "60000000-0000-0000-0000-000000000003",
            name: "Web Portal Bug",
          },
        ]),
      ),
      http.post("*/api/tickets", async ({ request }) => {
        requestBody = await request.json() as Record<string, unknown>;
        return HttpResponse.json({
          ...requestBody,
          assigneeId: null,
          assignedTeamId: "20000000-0000-0000-0000-000000000003",
          attachments: [],
          createdAt: "2026-05-30T08:00:00Z",
          customerId: "11111111-1111-1111-1111-111111111111",
          id: "22222222-2222-2222-2222-222222222222",
          productCode: "CORE",
          productName: "Core Platform",
          status: "NEW",
          ticketNumber: "TCK-2026-0044",
          updatedAt: "2026-05-30T08:00:00Z",
        });
      }),
    );

    renderPage();

    fireEvent.change(await screen.findByLabelText("Konu"), {
      target: { value: "Portal login fails" },
    });
    fireEvent.change(screen.getByLabelText("Kategori"), {
      target: { value: productId },
    });
    fireEvent.change(screen.getByLabelText("Talep tipi"), {
      target: { value: "WEB_PORTAL_BUG" },
    });
    fireEvent.change(screen.getByLabelText("Aciklama"), {
      target: { value: "Portal login flow returns a blank page after password entry." },
    });
    fireEvent.click(screen.getByRole("button", { name: "Gonder" }));

    await waitFor(() =>
      expect(requestBody).toMatchObject({
        description: "Portal login flow returns a blank page after password entry.",
        priority: "MEDIUM",
        productId,
        summary: "Portal login fails",
        topicCode: "WEB_PORTAL_BUG",
      }),
    );
  });
});
