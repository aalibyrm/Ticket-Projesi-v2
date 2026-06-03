import type { TicketPriority, TicketStatus } from "../api/mobileApiTypes";

export function formatDate(value: string | undefined) {
  if (!value) {
    return "-";
  }

  return new Intl.DateTimeFormat("tr-TR", {
    day: "2-digit",
    month: "short",
    year: "numeric"
  }).format(new Date(value));
}

export function formatTime(value: string | undefined) {
  if (!value) {
    return "-";
  }

  return new Intl.DateTimeFormat("tr-TR", {
    hour: "2-digit",
    minute: "2-digit"
  }).format(new Date(value));
}

export function priorityLabel(priority: TicketPriority) {
  return {
    HIGH: "Yuksek",
    LOW: "Dusuk",
    MEDIUM: "Normal"
  }[priority];
}

export function statusLabel(status: TicketStatus) {
  return {
    CLOSED: "Kapali",
    IN_PROGRESS: "Inceleniyor",
    NEW: "Yeni",
    RESOLVED: "Cozuldu",
    WAITING_FOR_CUSTOMER: "Beklemede"
  }[status];
}

export function minutesToShortDuration(value: number | string | undefined) {
  const minutes = Number(value ?? 0);

  if (!Number.isFinite(minutes) || minutes <= 0) {
    return "0dk";
  }

  const hours = Math.floor(minutes / 60);
  const remainingMinutes = Math.round(minutes % 60);

  if (hours <= 0) {
    return `${remainingMinutes}dk`;
  }

  return `${hours}s ${remainingMinutes}dk`;
}
