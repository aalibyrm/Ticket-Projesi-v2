import type { TicketPriority, TicketStatus } from "~/features/customer/customerTypes";
import { TmStatusChip } from "~/shared/design-system";

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
  const tone =
    status === "NEW" || status === "IN_PROGRESS"
      ? "active"
      : status === "WAITING_FOR_CUSTOMER"
        ? "waiting"
        : "resolved";

  return (
    <TmStatusChip
      label={statusLabels[status] ?? status}
      tone={tone}
    />
  );
}

export function PriorityChip({ priority }: { priority: TicketPriority }) {
  return (
    <TmStatusChip
      label={priorityLabels[priority] ?? priority}
      tone={priority === "HIGH" ? "danger" : "neutral"}
    />
  );
}
