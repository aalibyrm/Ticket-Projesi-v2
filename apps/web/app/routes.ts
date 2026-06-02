import { index, layout, route, type RouteConfig } from "@react-router/dev/routes";

export default [
  index("routes/home.tsx"),
  layout("routes/protected-layout.tsx", [
    route("tickets", "routes/tickets.tsx"),
    route("tickets/new", "routes/new-ticket.tsx"),
    route("tickets/:ticketId", "routes/ticket-detail.tsx"),
    route("notifications", "routes/notifications.tsx"),
    route("agent/inbox", "routes/agent-inbox.tsx"),
    route("reports", "routes/reports.tsx"),
  ]),
] satisfies RouteConfig;
