import type {
  DownloadUrlResponse,
  TicketAttachmentResponse,
  TicketCommentResponse,
  TicketPriority,
  TicketResponse,
  TicketStatus,
  TicketWorklogResponse,
} from "~/features/customer/customerTypes";

export type {
  DownloadUrlResponse,
  TicketAttachmentResponse,
  TicketCommentResponse,
  TicketPriority,
  TicketResponse,
  TicketStatus,
  TicketWorklogResponse,
};

export interface AssignTicketRequest {
  assigneeId: string;
  assignedTeamId?: string | null;
}

export interface ChangeTicketStatusRequest {
  status: TicketStatus;
}

export interface AddWorklogRequest {
  description: string;
  durationMinutes: number;
  workDate: string;
}

export interface AgentTicketFilters {
  priority: "ALL" | TicketPriority;
  search: string;
  status: "ALL" | "OPEN" | "CLOSED";
}
