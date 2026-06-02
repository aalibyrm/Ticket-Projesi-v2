import { Chip } from "@mui/material";
import type { TicketPriority, TicketStatus } from "~/features/customer/customerTypes";

const statusLabels: Record<TicketStatus, string> = {
  CLOSED: "Kapali",
  IN_PROGRESS: "Islemde",
  NEW: "Acik",
  RESOLVED: "Cozuldu",
  WAITING_FOR_CUSTOMER: "Musteri bekleniyor",
};

const priorityLabels: Record<TicketPriority, string> = {
  HIGH: "Yuksek",
  LOW: "Dusuk",
  MEDIUM: "Normal",
};

export function TicketStatusChip({ status }: { status: TicketStatus }) {
  const isActive = status === "NEW" || status === "IN_PROGRESS" || status === "WAITING_FOR_CUSTOMER";

  return (
    <Chip
      color={isActive ? "primary" : "default"}
      label={statusLabels[status] ?? status}
      size="small"
      variant={isActive ? "outlined" : "filled"}
    />
  );
}

export function PriorityChip({ priority }: { priority: TicketPriority }) {
  return (
    <Chip
      color={priority === "HIGH" ? "error" : "default"}
      label={priorityLabels[priority] ?? priority}
      size="small"
      variant={priority === "HIGH" ? "outlined" : "filled"}
    />
  );
}
