import type {
  DownloadUrlResponse,
  ConversationReadStateResponse,
  TicketAttachmentResponse,
  TicketCommentResponse,
  TicketPriority,
  TicketResponse,
  TicketStatus,
  TicketWorklogResponse,
} from "~/features/customer/customerTypes";

export type {
  DownloadUrlResponse,
  ConversationReadStateResponse,
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

export interface SupportTeamResponse {
  code: string;
  departmentCode: string;
  departmentId: string;
  id: string;
  leadActorId: string;
  name: string;
}

export interface TeamMemberResponse {
  actorId: string;
  teamCode: string;
  teamId: string;
  teamLead: boolean;
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
