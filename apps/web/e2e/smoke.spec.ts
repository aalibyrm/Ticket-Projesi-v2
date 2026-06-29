import { expect, test, type Page, type Route } from "@playwright/test";

const productId = "10000000-0000-4000-8000-000000000001";
const topicId = "10000000-0000-4000-8000-000000000002";
const ticketId = "10000000-0000-4000-8000-000000000056";
const teamId = "10000000-0000-4000-8000-000000000010";
const agentId = "00000000-0000-4000-8000-000000000056";
const departmentId = "10000000-0000-4000-8000-000000000011";
const fileId = "10000000-0000-4000-8000-000000000099";
const now = "2026-06-03T09:00:00Z";

type TicketStatus = "NEW" | "IN_PROGRESS" | "WAITING_FOR_CUSTOMER" | "RESOLVED" | "CLOSED";

interface DemoTicket {
  assigneeId?: string;
  assignedTeamId?: string;
  attachments: DemoAttachment[];
  createdAt: string;
  customerId: string;
  description: string;
  id: string;
  priority: "LOW" | "MEDIUM" | "HIGH";
  productCode: string;
  productId: string;
  productName: string;
  status: TicketStatus;
  summary: string;
  ticketNumber: string;
  updatedAt: string;
}

interface DemoAttachment {
  completedAt?: string;
  contentType: string;
  createdAt: string;
  id: string;
  originalFilename: string;
  sizeBytes: number;
  ticketId: string;
  uploadStatus: string;
  validationStatus: string;
}

interface DemoComment {
  authorId: string;
  body: string;
  createdAt: string;
  id: string;
  ticketId: string;
  visibility: "EXTERNAL" | "INTERNAL";
}

test("customer, agent, notification, and reporting smoke journey", async ({ page }) => {
  const state = createDemoState();
  await registerDemoApi(page, state);
  await registerE2eAuth(page);

  await page.goto("/tickets");
  await expect(page.getByRole("heading", { name: "Taleplerim" })).toBeVisible();
  await expect(page.getByRole("navigation").getByRole("link", { name: "Yeni talep" })).toHaveAttribute(
    "href",
    "/tickets/new",
  );

  await page.goto("/tickets/new");
  await expect(page.getByRole("heading", { name: "Yeni Destek Talebi" })).toBeVisible();
  await expect(page.getByTestId("create-ticket-form")).toHaveAttribute("data-e2e-ready", "true");

  await page.getByLabel("Konu").fill("VPN baglanti hatasi");
  await page.getByLabel("Kategori").selectOption(productId);
  await page.getByLabel("Talep tipi").selectOption("VPN_ACCESS");
  await page.getByRole("button", { name: "Yuksek" }).click();
  await page
    .getByLabel("Aciklama")
    .fill("VPN baglantisi mesai baslangicinda hata veriyor ve is akisimi durduruyor.");
  await page
    .locator("input[type='file']")
    .setInputFiles({
      buffer: Buffer.from("demo-log"),
      mimeType: "text/plain",
      name: "vpn-log.txt",
    });

  await expect(page.getByLabel("Konu")).toHaveValue("VPN baglanti hatasi");
  await expect(page.getByLabel("Kategori")).toHaveValue(productId);
  await expect(page.getByLabel("Talep tipi")).toHaveValue("VPN_ACCESS");
  await expect(page.getByLabel("Aciklama")).toHaveValue(
    "VPN baglantisi mesai baslangicinda hata veriyor ve is akisimi durduruyor.",
  );

  const createTicketResponse = page.waitForResponse(
    (response) =>
      response.url() === "http://localhost:8080/api/v1/tickets" &&
      response.request().method() === "POST" &&
      response.status() === 201,
  );
  const reserveUploadResponse = page.waitForResponse(
    (response) =>
      response.url() === "http://localhost:8080/api/v1/files/uploads" &&
      response.request().method() === "POST" &&
      response.status() === 200,
  );
  const objectStorageResponse = page.waitForResponse((response) => {
    const url = new URL(response.url());
    return (
      url.pathname === `/e2e-object-storage/${fileId}` &&
      response.request().method() === "PUT" &&
      response.status() === 200
    );
  });
  const completeUploadResponse = page.waitForResponse(
    (response) =>
      response.url() === `http://localhost:8080/api/v1/files/uploads/${fileId}/complete` &&
      response.request().method() === "POST" &&
      response.status() === 200,
  );

  await Promise.all([
    createTicketResponse,
    reserveUploadResponse,
    objectStorageResponse,
    completeUploadResponse,
    page.getByRole("button", { name: "Gonder" }).click(),
  ]);

  await expect(page).toHaveURL(new RegExp(`/tickets/${ticketId}$`));
  await expect(page.getByRole("heading", { name: "VPN baglanti hatasi" })).toBeVisible();
  await expect(page.getByText("vpn-log.txt")).toBeVisible();

  await page.goto("/agent/inbox");
  await expect(page.getByRole("heading", { name: "Atanan Biletler" })).toBeVisible();
  await page.getByText("VPN baglanti hatasi").first().click();
  await expect(page.getByRole("heading", { level: 5, name: "VPN baglanti hatasi" })).toBeVisible();

  await page.getByRole("button", { name: "Islemde yap" }).click();
  await expect(page.getByText("Islemde").first()).toBeVisible();

  await page
    .getByRole("textbox", { name: /Yanitinizi .* icin yazin/i })
    .fill("VPN profili yeniden olusturuldu, tekrar deneyebilir misiniz?");
  await page.getByRole("button", { name: "Yanitla" }).click();
  await expect(page.getByText("VPN profili yeniden olusturuldu")).toBeVisible();

  await page.getByLabel("Aciklama").fill("VPN profil reseti ve kullanici bilgilendirmesi yapildi.");
  await page.getByRole("button", { name: "Worklog ekle" }).click();
  await expect(page.getByText("VPN profil reseti")).toBeVisible();

  await page.goto("/notifications");
  await expect(page.getByRole("heading", { name: "Bildirimler" })).toBeVisible();
  await expect(page.getByText("Ticket guncellendi").first()).toBeVisible();
  await expect(page.getByText("E-posta kuyruya alindi").first()).toBeVisible();

  await page.goto("/reports");
  await expect(page.getByRole("heading", { name: "Operasyon gostergeleri" })).toBeVisible();
  await expect(page.getByText("SLA uyumu")).toBeVisible();
  await expect(page.getByText("Agent performansi")).toBeVisible();
});

function createDemoState() {
  return {
    comments: [] as DemoComment[],
    nextCommentNumber: 1,
    notifications: [
      {
        createdAt: now,
        id: "10000000-0000-4000-8000-000000000201",
        message: "Demo smoke baslangic bildirimi.",
        read: false,
        title: "Ticket platformu hazir",
        type: "TICKET_CREATED",
      },
    ],
    ticket: undefined as DemoTicket | undefined,
    worklogs: [] as unknown[],
  };
}

async function registerE2eAuth(page: Page) {
  await page.addInitScript(() => {
    window.localStorage.setItem("ticket:e2e-auth", "enabled");
    window.localStorage.setItem("ticket:e2e-auth:display-name", "E2E Admin");
    window.localStorage.setItem("ticket:e2e-auth:roles", "ADMIN,CUSTOMER,AGENT,MANAGER");
    window.localStorage.setItem("ticket:e2e-auth:user-id", "00000000-0000-4000-8000-000000000056");
    window.localStorage.setItem("ticket:e2e-auth:username", "e2e.admin");
  });
}

async function registerDemoApi(page: Page, state: ReturnType<typeof createDemoState>) {
  await page.route("http://localhost:8080/api/v1/**", async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const method = request.method();
    const path = url.pathname;

    if (method === "GET" && path === "/api/v1/products") {
      return json(route, [{ code: "NETWORK", id: productId, name: "Ag ve VPN" }]);
    }

    if (method === "GET" && path === "/api/v1/ticket-topics") {
      return json(route, [
        {
          code: "VPN_ACCESS",
          description: "VPN ve uzaktan erisim sorunlari",
          id: topicId,
          name: "VPN Erisim",
        },
      ]);
    }

    if (method === "GET" && path === "/api/v1/tickets") {
      return json(route, state.ticket ? [state.ticket] : []);
    }

    if (method === "POST" && path === "/api/v1/tickets") {
      const body = await request.postDataJSON();
      state.ticket = createTicket(body);
      return json(route, state.ticket, 201);
    }

    if (method === "POST" && path === "/api/v1/files/uploads") {
      const body = await request.postDataJSON();
      state.ticket?.attachments.push({
        contentType: body.contentType,
        createdAt: now,
        id: fileId,
        originalFilename: body.originalFilename,
        sizeBytes: body.sizeBytes,
        ticketId: body.ticketId,
        uploadStatus: "RESERVED",
        validationStatus: "PENDING",
      });
      return json(route, {
        expiresAt: "2026-06-03T09:15:00Z",
        fileId,
        method: "PUT",
        objectKey: `tickets/${ticketId}/${fileId}`,
        requiredHeaders: { "Content-Type": body.contentType },
        uploadUrl: `/e2e-object-storage/${fileId}`,
      });
    }

    if (method === "POST" && path === `/api/v1/files/uploads/${fileId}/complete`) {
      completeAttachment(state);
      return json(route, { status: "COMPLETED" });
    }

    if (method === "GET" && path === `/api/v1/tickets/${ticketId}`) {
      return json(route, requireTicket(state));
    }

    if (method === "GET" && path === `/api/v1/tickets/${ticketId}/comments`) {
      return json(route, state.comments.filter((comment) => comment.visibility === "EXTERNAL"));
    }

    if (method === "GET" && path === "/api/v1/agent/tickets") {
      return json(route, state.ticket ? [state.ticket] : []);
    }

    if (method === "GET" && path === `/api/v1/agent/tickets/${ticketId}`) {
      return json(route, requireTicket(state));
    }

    if (method === "GET" && path === `/api/v1/agent/tickets/${ticketId}/comments`) {
      return json(route, state.comments);
    }

    if (method === "PATCH" && path === `/api/v1/agent/tickets/${ticketId}/status`) {
      const body = await request.postDataJSON();
      const ticket = requireTicket(state);
      ticket.status = body.status;
      ticket.updatedAt = now;
      state.notifications.push({
        createdAt: now,
        id: "10000000-0000-4000-8000-000000000202",
        message: "E-posta kuyruya alindi ve musteri bilgilendirilecek.",
        read: false,
        ticketId,
        title: "Ticket guncellendi",
        type: "TICKET_STATUS_CHANGED",
      });
      return json(route, ticket);
    }

    if (method === "POST" && path === `/api/v1/agent/tickets/${ticketId}/comments/external`) {
      const body = await request.postDataJSON();
      const comment = addComment(state, body.body, "EXTERNAL");
      state.notifications.push({
        createdAt: now,
        id: "10000000-0000-4000-8000-000000000203",
        message: "E-posta kuyruya alindi ve yeni yanit bildirimi olustu.",
        read: false,
        ticketId,
        title: "Ticket guncellendi",
        type: "TICKET_EXTERNAL_COMMENT_ADDED",
      });
      return json(route, comment, 201);
    }

    if (method === "POST" && path === `/api/v1/agent/tickets/${ticketId}/comments/internal`) {
      const body = await request.postDataJSON();
      return json(route, addComment(state, body.body, "INTERNAL"), 201);
    }

    if (method === "GET" && path === "/api/v1/organization/teams") {
      return json(route, [
        {
          code: "NETOPS-L1",
          departmentCode: "IT",
          departmentId,
          id: teamId,
          leadActorId: agentId,
          name: "Network L1",
        },
      ]);
    }

    if (method === "GET" && path === `/api/v1/organization/teams/${teamId}/members`) {
      return json(route, [{ actorId: agentId, teamCode: "NETOPS-L1", teamId, teamLead: true }]);
    }

    if (method === "GET" && path === `/api/v1/agent/tickets/${ticketId}/worklogs`) {
      return json(route, state.worklogs);
    }

    if (method === "POST" && path === `/api/v1/agent/tickets/${ticketId}/worklogs`) {
      const body = await request.postDataJSON();
      const worklog = {
        agentId,
        createdAt: now,
        description: body.description,
        durationMinutes: Number(body.durationMinutes),
        id: `10000000-0000-4000-8000-00000000030${state.worklogs.length + 1}`,
        ticketId,
        workDate: body.workDate,
      };
      state.worklogs.push(worklog);
      return json(route, worklog, 201);
    }

    if (method === "GET" && path === "/api/v1/notifications") {
      return json(route, state.notifications);
    }

    if (method === "GET" && path === "/api/v1/reports/tickets/status-distribution") {
      return json(route, {
        counts: [{ count: 1, status: requireTicket(state).status }],
        departmentCounts: [
          {
            count: 1,
            routedDepartmentCode: "IT",
            routedDepartmentId: departmentId,
            routedDepartmentName: "Bilgi Teknolojileri",
          },
        ],
        generatedAt: now,
        teamCounts: [{ assignedTeamId: teamId, count: 1 }],
        totalOpenTickets: 1,
      });
    }

    if (method === "GET" && path === "/api/v1/reports/tickets/closed") {
      return json(route, {
        averageResolutionMinutes: 120,
        dailyCounts: [{ count: 1, date: "2026-06-03" }],
        fromDate: url.searchParams.get("fromDate") ?? "2026-05-05",
        generatedAt: now,
        priorityCounts: [{ count: 1, priority: "HIGH" }],
        toDate: url.searchParams.get("toDate") ?? "2026-06-03",
        totalClosedTickets: 1,
      });
    }

    if (method === "GET" && path === "/api/v1/reports/agents/performance") {
      return json(route, {
        generatedAt: now,
        rows: [
          {
            agentId,
            assignedTicketCount: 1,
            averageResolutionMinutes: 120,
            resolvedTicketCount: 1,
            totalWorklogMinutes: 30,
          },
        ],
      });
    }

    if (method === "GET" && path === "/api/v1/reports/sla/compliance") {
      return json(route, {
        activeTicketCount: 1,
        atRiskTicketCount: 0,
        breachedTicketCount: 0,
        compliancePercentage: 100,
        generatedAt: now,
        metTicketCount: 1,
        priorityBreakdown: [
          {
            activeTicketCount: 1,
            atRiskTicketCount: 0,
            breachedTicketCount: 0,
            compliancePercentage: 100,
            metTicketCount: 1,
            priority: "HIGH",
          },
        ],
      });
    }

    return json(route, { message: `Unhandled E2E API route: ${method} ${path}` }, 500);
  });

  await page.route("**/e2e-object-storage/**", (route) => route.fulfill({ status: 200 }));
}

function createTicket(body: Record<string, string>): DemoTicket {
  return {
    assigneeId: agentId,
    assignedTeamId: teamId,
    attachments: [],
    createdAt: now,
    customerId: "20000000-0000-4000-8000-000000000001",
    description: body.description,
    id: ticketId,
    priority: body.priority as DemoTicket["priority"],
    productCode: "NETWORK",
    productId,
    productName: "Ag ve VPN",
    status: "NEW",
    summary: body.summary,
    ticketNumber: "TCK-2026-00056",
    updatedAt: now,
  };
}

function completeAttachment(state: ReturnType<typeof createDemoState>) {
  const attachment = state.ticket?.attachments.find((item) => item.id === fileId);
  if (!attachment) {
    return;
  }

  attachment.completedAt = now;
  attachment.uploadStatus = "COMPLETED";
  attachment.validationStatus = "CLEAN";
}

function addComment(
  state: ReturnType<typeof createDemoState>,
  body: string,
  visibility: DemoComment["visibility"],
): DemoComment {
  const comment = {
    authorId: agentId,
    body,
    createdAt: now,
    id: `10000000-0000-4000-8000-00000000010${state.nextCommentNumber}`,
    ticketId,
    visibility,
  };
  state.nextCommentNumber += 1;
  state.comments.push(comment);
  return comment;
}

function requireTicket(state: ReturnType<typeof createDemoState>): DemoTicket {
  if (!state.ticket) {
    throw new Error("Demo ticket has not been created yet.");
  }

  return state.ticket;
}

function json(route: Route, value: unknown, status = 200) {
  return route.fulfill({
    contentType: "application/json",
    json: value,
    status,
  });
}
