export type TicketPriority = "LOW" | "MEDIUM" | "HIGH";
export type TicketStatus = "NEW" | "IN_PROGRESS" | "WAITING_FOR_CUSTOMER" | "RESOLVED" | "CLOSED";

export interface ProductResponse {
  code: string;
  id: string;
  name: string;
}

export interface TicketTopicResponse {
  code: string;
  description: string;
  id: string;
  name: string;
}

export interface TicketAttachmentResponse {
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

export interface TicketResponse {
  assigneeId?: string;
  assignedTeamId?: string;
  attachments: TicketAttachmentResponse[];
  createdAt: string;
  customerId: string;
  description: string;
  id: string;
  priority: TicketPriority;
  productCode: string;
  productId: string;
  productName: string;
  status: TicketStatus;
  summary: string;
  ticketNumber: string;
  updatedAt: string;
}

export interface TicketCommentResponse {
  authorId: string;
  body: string;
  createdAt: string;
  id: string;
  ticketId: string;
  visibility: "EXTERNAL" | "INTERNAL";
}

export interface CreateTicketRequest {
  description: string;
  priority: TicketPriority;
  productId: string;
  summary: string;
  topicCode: string;
}

export interface NotificationResponse {
  createdAt: string;
  id: string;
  message: string;
  read: boolean;
  ticketId?: string | null;
  title: string;
  type: string;
}

export interface UploadUrlResponse {
  expiresAt: string;
  fileId: string;
  method: string;
  objectKey: string;
  requiredHeaders: Record<string, string>;
  uploadUrl: string;
}

export interface MobilePickedFile {
  mimeType?: string;
  name: string;
  size: number;
  uri: string;
}

export interface AssignTicketRequest {
  assigneeId: string;
  assignedTeamId?: string | null;
}

export interface ChangeTicketStatusRequest {
  status: TicketStatus;
}

export interface AgentPerformanceRowResponse {
  agentId: string;
  assignedTicketCount: number;
  averageResolutionMinutes: number | string;
  resolvedTicketCount: number;
  totalWorklogMinutes: number;
}

export interface AgentPerformanceReportResponse {
  generatedAt: string;
  rows: AgentPerformanceRowResponse[];
}

export interface ClosedTicketDateRangeResponse {
  averageResolutionMinutes: number | string;
  dailyCounts: { count: number; date: string }[];
  fromDate: string;
  generatedAt: string;
  priorityCounts: { count: number; priority: string }[];
  toDate: string;
  totalClosedTickets: number;
}

export interface SlaComplianceReportResponse {
  activeTicketCount: number;
  atRiskTicketCount: number;
  breachedTicketCount: number;
  compliancePercentage: number | string;
  generatedAt: string;
  metTicketCount: number;
  priorityBreakdown: {
    activeTicketCount: number;
    atRiskTicketCount: number;
    breachedTicketCount: number;
    compliancePercentage: number | string;
    metTicketCount: number;
    priority: string;
  }[];
}

export interface TicketStatusDistributionResponse {
  counts: { count: number; status: string }[];
  departmentCounts: {
    count: number;
    routedDepartmentCode?: string | null;
    routedDepartmentId: string;
    routedDepartmentName?: string | null;
  }[];
  generatedAt: string;
  teamCounts: { assignedTeamId: string; count: number }[];
  totalOpenTickets: number;
}
