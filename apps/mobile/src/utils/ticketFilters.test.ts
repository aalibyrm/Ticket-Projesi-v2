import type { TicketResponse } from "../api/mobileApiTypes";
import { filterTicketsByPriority, filterTicketsBySearch, filterTicketsByStatus } from "./ticketFilters";

const tickets = [
  createTicket("1", "Odeme sorunu", "HIGH", "NEW"),
  createTicket("2", "Fatura adresi", "MEDIUM", "WAITING_FOR_CUSTOMER"),
  createTicket("3", "Profil hatasi", "LOW", "CLOSED")
];

describe("ticket filters", () => {
  it("filters open tickets without closed states", () => {
    expect(filterTicketsByStatus(tickets, "OPEN").map((ticket) => ticket.id)).toEqual(["1", "2"]);
  });

  it("filters pending tickets", () => {
    expect(filterTicketsByStatus(tickets, "PENDING").map((ticket) => ticket.id)).toEqual(["2"]);
  });

  it("filters by priority", () => {
    expect(filterTicketsByPriority(tickets, "HIGH").map((ticket) => ticket.id)).toEqual(["1"]);
  });

  it("filters by searchable ticket content", () => {
    expect(filterTicketsBySearch(tickets, "profil").map((ticket) => ticket.id)).toEqual(["3"]);
  });
});

function createTicket(
  id: string,
  summary: string,
  priority: TicketResponse["priority"],
  status: TicketResponse["status"]
): TicketResponse {
  return {
    attachments: [],
    createdAt: "2026-06-01T10:00:00Z",
    customerId: "customer",
    description: `${summary} aciklama`,
    id,
    priority,
    productCode: "CORE",
    productId: "product",
    productName: "Core",
    status,
    summary,
    ticketNumber: `TKT-${id}`,
    updatedAt: "2026-06-01T10:00:00Z"
  };
}
