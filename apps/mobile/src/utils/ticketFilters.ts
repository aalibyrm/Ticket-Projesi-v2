import type { TicketPriority, TicketResponse, TicketStatus } from "../api/mobileApiTypes";

export type TicketStatusFilter = "ALL" | "OPEN" | "PENDING" | "CLOSED";
export type TicketPriorityFilter = "ALL" | TicketPriority;

const pendingStatuses: TicketStatus[] = ["WAITING_FOR_CUSTOMER"];
const closedStatuses: TicketStatus[] = ["CLOSED", "RESOLVED"];

export function filterTicketsByStatus(tickets: TicketResponse[], filter: TicketStatusFilter) {
  if (filter === "ALL") {
    return tickets;
  }

  if (filter === "OPEN") {
    return tickets.filter((ticket) => !closedStatuses.includes(ticket.status));
  }

  if (filter === "PENDING") {
    return tickets.filter((ticket) => pendingStatuses.includes(ticket.status));
  }

  return tickets.filter((ticket) => closedStatuses.includes(ticket.status));
}

export function filterTicketsByPriority(tickets: TicketResponse[], filter: TicketPriorityFilter) {
  if (filter === "ALL") {
    return tickets;
  }

  return tickets.filter((ticket) => ticket.priority === filter);
}

export function filterTicketsBySearch(tickets: TicketResponse[], search: string) {
  const normalizedSearch = search.trim().toLowerCase();

  if (!normalizedSearch) {
    return tickets;
  }

  return tickets.filter((ticket) =>
    [ticket.summary, ticket.description, ticket.ticketNumber, ticket.productName]
      .filter(Boolean)
      .some((value) => value.toLowerCase().includes(normalizedSearch))
  );
}
