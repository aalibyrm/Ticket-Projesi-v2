import { expect, test, type Browser, type Page } from "@playwright/test";

const apiBaseUrl = process.env.VITE_API_BASE_URL ?? "http://localhost:8088";
const mailpitApiUrl = process.env.MAILPIT_API_URL ?? "http://localhost:8025/api/v1/messages";
const prometheusApiUrl = process.env.PROMETHEUS_API_URL ?? "http://localhost:9090/api/v1/targets";
const grafanaHealthUrl = process.env.GRAFANA_HEALTH_URL ?? "http://localhost:3001/api/health";
const jaegerServicesUrl = process.env.JAEGER_SERVICES_URL ?? "http://localhost:16686/api/services";
const openSearchDashboardsStatusUrl =
  process.env.OPENSEARCH_DASHBOARDS_STATUS_URL ?? "http://localhost:5601/api/status";

const password = process.env.FULLSTACK_E2E_PASSWORD ?? "Password123!";
const runId = `E2E-${Date.now()}`;

const users = {
  customer: "customer.user",
  paymentAgent: "agent.payment",
  webAgent: "agent.web",
  coreAgent: "agent.core",
  manager: "manager.user",
};

interface AuthSession {
  displayName: string;
  email?: string;
  page: Page;
  token: string;
  userId: string;
  username?: string;
}

interface ProductResponse {
  code: string;
  id: string;
  name: string;
}

interface TicketTopicResponse {
  code: string;
  id: string;
  name: string;
}

interface TicketAttachmentResponse {
  id: string;
  originalFilename: string;
  uploadStatus: string;
  validationStatus: string;
}

interface TicketResponse {
  assigneeId?: string | null;
  assignedTeamId?: string | null;
  attachments: TicketAttachmentResponse[];
  customerId: string;
  description: string;
  id: string;
  priority: "LOW" | "MEDIUM" | "HIGH";
  productId: string;
  summary: string;
  ticketNumber: string;
  status: "NEW" | "IN_PROGRESS" | "WAITING_FOR_CUSTOMER" | "RESOLVED" | "CLOSED";
}

interface TicketCommentResponse {
  authorId: string;
  body: string;
  visibility: "EXTERNAL" | "INTERNAL";
}

interface NotificationResponse {
  id: string;
  ticketId?: string | null;
  title: string;
  message: string;
  read: boolean;
}

interface SupportTeamResponse {
  code: string;
  id: string;
}

interface ApiResult<T> {
  body: T;
  rawText: string;
  status: number;
}

interface Catalog {
  products: ProductResponse[];
  topics: TicketTopicResponse[];
}

let customer: AuthSession;
let paymentAgent: AuthSession;
let webAgent: AuthSession;
let coreAgent: AuthSession;
let manager: AuthSession;
let catalog: Catalog;
let paymentTicket: TicketResponse;
let paymentAttachmentId: string;

test.describe.serial("final full-stack acceptance", () => {
  test.beforeAll(async ({ browser }) => {
    customer = await loginAs(browser, users.customer, "/tickets");
    paymentAgent = await loginAs(browser, users.paymentAgent, "/agent/inbox");
    webAgent = await loginAs(browser, users.webAgent, "/agent/inbox");
    coreAgent = await loginAs(browser, users.coreAgent, "/agent/inbox");
    manager = await loginAs(browser, users.manager, "/reports");
    catalog = {
      products: await api<ProductResponse[]>(customer.token, "GET", "/api/v1/products"),
      topics: await api<TicketTopicResponse[]>(customer.token, "GET", "/api/v1/ticket-topics"),
    };
  });

  test.afterAll(async () => {
    await Promise.all([
      customer?.page.context().close(),
      paymentAgent?.page.context().close(),
      webAgent?.page.context().close(),
      coreAgent?.page.context().close(),
      manager?.page.context().close(),
    ]);
  });

  test("role-based access gates customer and agent portals", async () => {
    await customer.page.goto("/tickets");
    await expect(customer.page.getByRole("heading", { name: "Taleplerim" })).toBeVisible();
    await assertForbiddenOrRedirect(customer.page, "/agent/inbox", /\/tickets/);

    await paymentAgent.page.goto("/agent/inbox");
    await expect(paymentAgent.page.getByRole("heading", { name: "Is kuyrugu" })).toBeVisible();
    await assertForbiddenOrRedirect(paymentAgent.page, "/tickets", /\/agent\/inbox/);
  });

  test("customer creates a payment ticket with a real attachment and payment routing", async () => {
    const summary = `${runId} payment failure attachment`;
    const description = `${summary} description with enough detail for validation.`;
    paymentTicket = await createTicketWithAttachment(customer.page, catalog, {
      description,
      filename: `${runId}-payment-proof.txt`,
      priorityLabel: "Yuksek",
      summary,
      topicCode: "PAYMENT_FAILURE",
    });
    paymentAttachmentId = paymentTicket.attachments[0]?.id;

    expect(paymentAttachmentId).toBeTruthy();
    expect(paymentTicket.attachments[0]?.uploadStatus).toBe("COMPLETED");
    await customer.page.goto("/tickets");
    await expect(customer.page.getByText(summary)).toBeVisible();

    await expect.poll(
      async () => hasTicket(paymentAgent.token, paymentTicket.id),
      { timeout: 30_000 },
    ).toBe(true);
    await expect.poll(
      async () => hasTicket(webAgent.token, paymentTicket.id),
      { timeout: 10_000 },
    ).toBe(false);

    await expect.poll(
      async () => mailpitContainsAnyRecipient(ticketCreatedSupportEmails(), paymentTicket.ticketNumber),
      { timeout: 45_000 },
    ).toBe(true);
  });

  test("topic routing sends web and core tickets to their teams", async () => {
    const webTicket = await createTicketViaApi(customer.token, "WEB_PORTAL_BUG", `${runId} web portal issue`);
    const coreTicket = await createTicketViaApi(customer.token, "CORE_SYSTEM_ERROR", `${runId} core system issue`);

    await expect.poll(async () => hasTicket(webAgent.token, webTicket.id), { timeout: 30_000 }).toBe(true);
    await expect.poll(async () => hasTicket(coreAgent.token, coreTicket.id), { timeout: 30_000 }).toBe(true);
    await expect.poll(async () => hasTicket(paymentAgent.token, webTicket.id), { timeout: 10_000 }).toBe(false);
  });

  test("attachment authorization allows owner and routed team while denying cross-team access", async () => {
    const customerDownload = await apiResult(customer.token, "POST", `/api/v1/files/${paymentAttachmentId}/download-url`);
    const paymentDownload = await apiResult(paymentAgent.token, "POST", `/api/v1/files/${paymentAttachmentId}/download-url`);
    const webDownload = await apiResult(webAgent.token, "POST", `/api/v1/files/${paymentAttachmentId}/download-url`);
    const webTicketDetail = await apiResult(webAgent.token, "GET", `/api/v1/agent/tickets/${paymentTicket.id}`);

    expect(customerDownload.status).toBe(200);
    expect(paymentDownload.status).toBe(200);
    expect([403, 404]).toContain(webDownload.status);
    expect([403, 404]).toContain(webTicketDetail.status);
  });

  test("messages, internal notes, assignment, status, worklog, notifications, and live badge work", async () => {
    const customerMessage = `${runId} customer asks for an update`;
    const agentReply = `${runId} payment agent external reply`;
    const internalNote = `${runId} internal note hidden from customer`;
    const worklogDescription = `${runId} investigation worklog`;
    const beforeUnread = await notificationUnreadCount(customer.page);

    await api<TicketCommentResponse>(
      customer.token,
      "POST",
      `/api/v1/tickets/${paymentTicket.id}/comments/external`,
      { body: customerMessage },
    );

    await paymentAgent.page.goto(`/agent/tickets/${paymentTicket.id}`);
    await expect(paymentAgent.page.getByText(customerMessage)).toBeVisible();
    await expect(paymentAgent.page.getByText(displayNameForActor(paymentTicket.customerId, "Musteri")).first())
      .toBeVisible();
    await expect(paymentAgent.page.getByText(paymentTicket.customerId)).toHaveCount(0);

    await paymentAgent.page.getByLabel("Ic not").fill(internalNote);
    await Promise.all([
      paymentAgent.page.waitForResponse((response) =>
        response.url().includes(`/api/v1/agent/tickets/${paymentTicket.id}/comments/internal`)
          && response.status() === 201,
      ),
      paymentAgent.page.getByRole("button", { name: "Ic not ekle" }).click(),
    ]);

    await gotoCustomerTicketDetail(customer.page, paymentTicket.id);
    await expect(customer.page.getByText(internalNote)).toHaveCount(0);

    await paymentAgent.page.goto(`/agent/tickets/${paymentTicket.id}`);
    await expect(paymentAgent.page.getByText(internalNote)).toBeVisible();

    await Promise.all([
      paymentAgent.page.waitForResponse((response) =>
        response.url().includes(`/api/v1/agent/tickets/${paymentTicket.id}/assignment`)
          && response.status() === 200,
      ),
      paymentAgent.page.getByRole("button", { name: "Bana ata" }).click(),
    ]);
    let updatedTicket = await api<TicketResponse>(paymentAgent.token, "GET", `/api/v1/agent/tickets/${paymentTicket.id}`);
    expect(updatedTicket.assigneeId).toBe(paymentAgent.userId);

    await paymentAgent.page.getByLabel("Musteriye yanit").fill(agentReply);
    await Promise.all([
      paymentAgent.page.waitForResponse((response) =>
        response.url().includes(`/api/v1/agent/tickets/${paymentTicket.id}/comments/external`)
          && response.status() === 201,
      ),
      paymentAgent.page.getByRole("button", { name: "Yanitla" }).click(),
    ]);

    await gotoCustomerTicketDetail(customer.page, paymentTicket.id);
    await expect(customer.page.getByText(agentReply)).toBeVisible();
    await expect(customer.page.getByText(internalNote)).toHaveCount(0);

    await paymentAgent.page.goto(`/agent/tickets/${paymentTicket.id}`);

    await Promise.all([
      paymentAgent.page.waitForResponse((response) =>
        response.url().includes(`/api/v1/agent/tickets/${paymentTicket.id}/status`)
          && response.status() === 200,
      ),
      paymentAgent.page.getByRole("button", { name: "Status guncelle" }).click(),
    ]);
    updatedTicket = await api<TicketResponse>(paymentAgent.token, "GET", `/api/v1/agent/tickets/${paymentTicket.id}`);
    expect(updatedTicket.status).toBe("IN_PROGRESS");

    const sidePanel = paymentAgent.page.locator("aside");
    await sidePanel.getByLabel("Sure").fill("30");
    await sidePanel.getByLabel("Aciklama").fill(worklogDescription);
    await Promise.all([
      paymentAgent.page.waitForResponse((response) =>
        response.url().includes(`/api/v1/agent/tickets/${paymentTicket.id}/worklogs`)
          && response.status() === 201,
      ),
      sidePanel.getByRole("button", { name: "Worklog ekle" }).click(),
    ]);
    await expect(paymentAgent.page.getByText(worklogDescription)).toBeVisible();

    await expect.poll(
      async () => hasNotification(customer.token, paymentTicket.id),
      { timeout: 30_000 },
    ).toBe(true);
    await expect.poll(
      async () => mailpitContainsAnyRecipient(customerEmails(), paymentTicket.ticketNumber),
      { timeout: 45_000 },
    ).toBe(true);

    await customer.page.goto("/notifications");
    await openNotificationAndNavigate(customer.page, paymentTicket.id);

    const liveUpdateMessage = `${runId} live badge message`;
    await paymentAgent.page.goto(`/agent/tickets/${paymentTicket.id}`);
    await paymentAgent.page.getByLabel("Musteriye yanit").fill(liveUpdateMessage);
    await Promise.all([
      paymentAgent.page.waitForResponse((response) =>
        response.url().includes(`/api/v1/agent/tickets/${paymentTicket.id}/comments/external`)
          && response.status() === 201,
      ),
      paymentAgent.page.getByRole("button", { name: "Yanitla" }).click(),
    ]);

    await expect.poll(
      async () => notificationUnreadCount(customer.page),
      { timeout: 20_000 },
    ).toBeGreaterThan(beforeUnread);
  });

  test("manager reports are visible only to manager roles", async () => {
    await manager.page.goto("/reports");
    await expect(manager.page.getByRole("heading", { name: "Operasyon gostergeleri" })).toBeVisible();
    await expect(manager.page.getByText("Status dagilimi")).toBeVisible();
    await expect(manager.page.getByText("Kapanis hacmi")).toBeVisible();
    await expect(manager.page.getByText("SLA dagilimi")).toBeVisible();
    await expect(manager.page.getByText("Agent performansi")).toBeVisible();

    await assertForbiddenOrRedirect(customer.page, "/reports", /\/tickets/);
    await assertForbiddenOrRedirect(paymentAgent.page, "/reports", /\/agent\/inbox/);
  });

  test("swagger, API versioning, validation, and observability endpoints are controlled", async () => {
    const swaggerUi = await fetch(`${apiBaseUrl}/swagger-ui.html`);
    expect(swaggerUi.ok).toBe(true);

    for (const service of [
      "ticket-service",
      "file-service",
      "workflow-sla-service",
      "notification-service",
      "reporting-service",
    ]) {
      const spec = await fetchJson<{ paths: Record<string, unknown> }>(`${apiBaseUrl}/v3/api-docs/${service}`);
      const paths = Object.keys(spec.paths ?? {});
      const leakedInternalPaths = paths.filter((path) =>
        path === "/internal" || path.startsWith("/internal/") || path.startsWith("/api/internal")
          || path.startsWith("/api/v1/internal"),
      );
      expect(leakedInternalPaths, `${service} leaked internal paths`).toEqual([]);
      expect(paths.filter((path) => path.startsWith("/api/")).every((path) => path.startsWith("/api/v1/"))).toBe(true);
    }

    const validationTicket = await createTicketViaApi(customer.token, "PAYMENT_FAILURE", `${runId} validation target`);
    await expectControlledError(
      apiResult(customer.token, "POST", "/api/v1/tickets", validCreateTicketPayload("PAYMENT_FAILURE", "")),
      [400],
    );
    await expectControlledError(
      apiResult(
        customer.token,
        "POST",
        "/api/v1/tickets",
        validCreateTicketPayload("PAYMENT_FAILURE", `${runId} missing description`, ""),
      ),
      [400],
    );
    await expectControlledError(
      apiResult(customer.token, "POST", "/api/v1/files/uploads", {
        contentType: "application/x-msdownload",
        originalFilename: "malware.exe",
        sizeBytes: 120,
        ticketId: validationTicket.id,
      }),
      [400],
    );
    await expectControlledError(
      apiResult(customer.token, "POST", "/api/v1/files/uploads", {
        contentType: "text/plain",
        originalFilename: "large.txt",
        sizeBytes: 10_485_761,
        ticketId: validationTicket.id,
      }),
      [400],
    );
    await expectControlledError(
      apiResult(paymentAgent.token, "POST", `/api/v1/agent/tickets/${paymentTicket.id}/worklogs`, {
        description: `${runId} negative duration`,
        durationMinutes: -1,
        workDate: todayIsoDate(),
      }),
      [400],
    );
    await expectControlledError(apiResult(undefined, "GET", "/api/v1/tickets"), [401, 403]);

    await expectServiceReachable(prometheusApiUrl, "prometheus targets API");
    await expectServiceReachable(grafanaHealthUrl, "grafana health API");
    await expectServiceReachable(jaegerServicesUrl, "jaeger services API");
    await expectServiceReachable(openSearchDashboardsStatusUrl, "opensearch dashboards status API");
  });
});

async function loginAs(browser: Browser, username: string, targetPath: string): Promise<AuthSession> {
  const context = await browser.newContext();
  const page = await context.newPage();
  let token: string | undefined;

  page.on("request", (request) => {
    if (!request.url().startsWith(apiBaseUrl)) {
      return;
    }
    const authorization = request.headers().authorization;
    if (authorization?.startsWith("Bearer ")) {
      token = authorization.slice("Bearer ".length);
    }
  });

  await page.goto("/");
  await page.getByRole("button", { name: /Giris Yap|Login|Sign in/i }).click();
  await page.locator("#username").fill(username);
  await page.locator("#password").fill(password);
  await page.locator("#kc-login").click();
  await page.waitForLoadState("domcontentloaded");
  await page.goto(targetPath);
  await expect.poll(() => token, { timeout: 30_000 }).toBeTruthy();

  const claims = claimsFromJwt(token as string);
  return {
    displayName: claims.name ?? claims.preferred_username ?? claims.sub,
    email: claims.email,
    page,
    token: token as string,
    userId: claims.sub,
    username: claims.preferred_username,
  };
}

async function createTicketWithAttachment(
  page: Page,
  currentCatalog: Catalog,
  options: {
    description: string;
    filename: string;
    priorityLabel: string;
    summary: string;
    topicCode: string;
  },
): Promise<TicketResponse> {
  await page.goto("/tickets/new");
  await page.getByLabel("Konu").fill(options.summary);
  await page.getByLabel("Kategori").selectOption(productForTopic(currentCatalog, options.topicCode).id);
  await page.getByLabel("Talep tipi").selectOption(options.topicCode);
  await page.getByLabel(options.priorityLabel).check();
  await page.getByLabel("Aciklama").fill(options.description);
  await page.locator("input[type='file']").setInputFiles({
    buffer: Buffer.from(`error ${options.summary} ${new Date().toISOString()}`, "utf-8"),
    mimeType: "text/plain",
    name: options.filename,
  });

  await Promise.all([
    page.waitForURL(/\/tickets\/[0-9a-f-]+$/i, { timeout: 60_000 }),
    page.getByRole("button", { name: "Gonder" }).click(),
  ]);
  await expect(page.getByText(options.filename)).toBeVisible();

  const ticketId = ticketIdFromUrl(page);
  const ticket = await api<TicketResponse>(customer.token, "GET", `/api/v1/tickets/${ticketId}`);
  expect(ticket.summary).toBe(options.summary);
  expect(ticket.attachments.some((attachment) => attachment.originalFilename === options.filename)).toBe(true);
  return ticket;
}

async function createTicketViaApi(token: string, topicCode: string, summary: string): Promise<TicketResponse> {
  return api<TicketResponse>(token, "POST", "/api/v1/tickets", validCreateTicketPayload(topicCode, summary));
}

function validCreateTicketPayload(topicCode: string, summary: string, description?: string) {
  return {
    description: description ?? `${summary} full-stack acceptance description with enough detail.`,
    priority: "HIGH",
    productId: productForTopic(catalog, topicCode).id,
    summary,
    topicCode,
  };
}

function productForTopic(currentCatalog: Catalog, topicCode: string): ProductResponse {
  const preferredCodeByTopic: Record<string, string[]> = {
    CORE_SYSTEM_ERROR: ["CORE_SYSTEM", "BACKEND_PLATFORM", "MOBILE_APP"],
    PAYMENT_FAILURE: ["MOBILE_APP", "FINANCE_PORTAL", "PAYMENTS"],
    WEB_PORTAL_BUG: ["WEB_PORTAL", "WEB_APP", "MOBILE_APP"],
  };
  const preferredCodes = preferredCodeByTopic[topicCode] ?? [];
  return currentCatalog.products.find((product) => preferredCodes.includes(product.code))
    ?? currentCatalog.products[0];
}

async function hasTicket(token: string, ticketId: string): Promise<boolean> {
  const response = await apiResult<TicketResponse[]>(token, "GET", "/api/v1/agent/tickets");
  if (response.status !== 200 || !Array.isArray(response.body)) {
    return false;
  }
  return response.body.some((ticket) => ticket.id === ticketId);
}

async function hasNotification(token: string, ticketId: string): Promise<boolean> {
  const notifications = await api<NotificationResponse[]>(token, "GET", "/api/v1/notifications");
  return notifications.some((notification) => notification.ticketId === ticketId);
}

async function openNotificationAndNavigate(page: Page, ticketId: string): Promise<void> {
  await page.goto("/notifications");
  await page.getByText(paymentTicket.ticketNumber).first().click();
  await expect(page).toHaveURL(new RegExp(`/tickets/${ticketId}$`));
  await expect(page.getByRole("heading", { name: paymentTicket.summary })).toBeVisible();
}

async function gotoCustomerTicketDetail(page: Page, ticketId: string): Promise<void> {
  const responsePromise = page.waitForResponse((response) =>
    response.url().includes(`/api/v1/tickets/${ticketId}`) && response.request().method() === "GET",
  );
  await page.goto(`/tickets/${ticketId}`);
  const response = await responsePromise;
  expect(response.status(), `GET /api/v1/tickets/${ticketId} returned ${response.status()}`).toBe(200);
  await expect(page.getByRole("heading", { name: paymentTicket.summary })).toBeVisible();
}

async function assertForbiddenOrRedirect(page: Page, path: string, expectedUrl: RegExp): Promise<void> {
  await page.goto(path);
  await expect(page).toHaveURL(expectedUrl);
}

async function api<T>(token: string | undefined, method: string, path: string, body?: unknown): Promise<T> {
  const result = await apiResult<T>(token, method, path, body);
  expect(result.status, `${method} ${path}: ${result.rawText}`).toBeGreaterThanOrEqual(200);
  expect(result.status, `${method} ${path}: ${result.rawText}`).toBeLessThan(300);
  return result.body;
}

async function apiResult<T = unknown>(
  token: string | undefined,
  method: string,
  path: string,
  body?: unknown,
): Promise<ApiResult<T>> {
  const response = await fetch(`${apiBaseUrl}${path}`, {
    body: body === undefined ? undefined : JSON.stringify(body),
    headers: {
      ...(body === undefined ? {} : { "Content-Type": "application/json" }),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    method,
  });
  const rawText = await response.text();
  const parsedBody = rawText ? safeJsonParse(rawText) : undefined;
  return {
    body: parsedBody as T,
    rawText,
    status: response.status,
  };
}

async function fetchJson<T>(url: string): Promise<T> {
  const response = await fetch(url);
  expect(response.ok, `${url} returned ${response.status}`).toBe(true);
  return response.json() as Promise<T>;
}

async function expectControlledError(resultPromise: Promise<ApiResult<unknown>>, expectedStatuses: number[]) {
  const result = await resultPromise;
  expect(expectedStatuses).toContain(result.status);
  expect(result.rawText).not.toMatch(/Exception|stackTrace|Authorization|Bearer|R2_SECRET|SECRET_ACCESS_KEY/i);
}

async function mailpitContains(expectedFragments: string[]): Promise<boolean> {
  const response = await fetch(mailpitApiUrl);
  if (!response.ok) {
    return false;
  }
  const payload = JSON.stringify(await response.json());
  return expectedFragments.every((fragment) => payload.includes(fragment));
}

async function mailpitContainsAnyRecipient(candidateEmails: string[], ticketNumber: string): Promise<boolean> {
  const response = await fetch(mailpitApiUrl);
  if (!response.ok) {
    return false;
  }
  const payload = JSON.stringify(await response.json());
  return candidateEmails.some((email) => payload.includes(email)) && payload.includes(ticketNumber);
}

async function notificationUnreadCount(page: Page): Promise<number> {
  const labels = await page.locator("[aria-label^='Bildirimler']").evaluateAll((elements) =>
    elements
      .map((element) => element.getAttribute("aria-label") ?? "")
      .filter(Boolean),
  );
  return labels.reduce((max, label) => {
    const match = /(\d+) okunmamis/.exec(label);
    return Math.max(max, match ? Number(match[1]) : 0);
  }, 0);
}

async function expectServiceReachable(url: string, label: string): Promise<void> {
  const response = await fetch(url);
  expect(response.ok, `${label} is not reachable at ${url}; manual evidence may be BLOCKED`).toBe(true);
}

function ticketIdFromUrl(page: Page): string {
  const match = /\/tickets\/([0-9a-f-]+)$/i.exec(page.url());
  if (!match) {
    throw new Error(`Ticket id could not be parsed from ${page.url()}`);
  }
  return match[1];
}

function todayIsoDate(): string {
  return new Date().toISOString().slice(0, 10);
}

function claimsFromJwt(token: string): { email?: string; name?: string; preferred_username?: string; sub: string } {
  const [, payload] = token.split(".");
  if (!payload) {
    throw new Error("JWT payload is missing");
  }
  const normalizedPayload = payload.replace(/-/g, "+").replace(/_/g, "/");
  const decodedPayload = JSON.parse(Buffer.from(normalizedPayload, "base64").toString("utf-8")) as {
    email?: string;
    name?: string;
    preferred_username?: string;
    sub?: string;
  };
  if (!decodedPayload.sub) {
    throw new Error("JWT subject is missing");
  }
  return { ...decodedPayload, sub: decodedPayload.sub };
}

function displayNameForActor(actorId: string, fallbackLabel: string): string {
  const knownNames: Record<string, string> = {
    "80000000-0000-0000-0000-000000000001": "Demo Customer",
    "40000000-0000-0000-0000-000000000003": "Web Agent",
    "40000000-0000-0000-0000-000000000004": "Core Agent",
    "40000000-0000-0000-0000-000000000008": "Payment Agent",
  };
  return knownNames[actorId] ?? `${fallbackLabel} ${actorId.slice(0, 8)}`;
}

function emailForActor(actorId: string): string {
  const knownEmails: Record<string, string> = {
    "80000000-0000-0000-0000-000000000001": "customer.user@example.local",
    "40000000-0000-0000-0000-000000000003": "agent.web@example.local",
    "40000000-0000-0000-0000-000000000004": "agent.core@example.local",
    "40000000-0000-0000-0000-000000000008": "agent.payment@example.local",
  };
  return knownEmails[actorId] ?? `user-${actorId.slice(0, 8).toLowerCase()}@example.local`;
}

function ticketCreatedSupportEmails(): string[] {
  return uniqueEmails([
    paymentAgent.email,
    emailForActor(paymentAgent.userId),
    "agent.payment@example.local",
  ]);
}

function customerEmails(): string[] {
  return uniqueEmails([
    customer.email,
    emailForActor(paymentTicket.customerId),
    "customer.user@example.local",
  ]);
}

function uniqueEmails(emails: Array<string | undefined>): string[] {
  return [...new Set(emails.filter((email): email is string => Boolean(email)))];
}

function safeJsonParse(rawText: string): unknown {
  try {
    return JSON.parse(rawText);
  } catch {
    return rawText;
  }
}
