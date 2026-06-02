export type TicketPriority = "LOW" | "MEDIUM" | "HIGH";
export type TicketStatus = "NEW" | "IN_PROGRESS" | "WAITING_FOR_CUSTOMER" | "RESOLVED" | "CLOSED";

export interface ProductResponse {
  code: string;
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

export interface TicketWorklogResponse {
  agentId: string;
  createdAt: string;
  description: string;
  durationMinutes: number;
  id: string;
  ticketId: string;
  workDate: string;
}

export interface CreateTicketRequest {
  description: string;
  priority: TicketPriority;
  productId: string;
  summary: string;
}

export interface CreateTicketFormValues extends CreateTicketRequest {
  attachment?: FileList;
}

export interface NotificationResponse {
  createdAt: string;
  id: string;
  message: string;
  read: boolean;
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

export interface DownloadUrlResponse {
  downloadUrl: string;
  expiresAt: string;
  fileId: string;
  method: string;
  requiredHeaders: Record<string, string>;
}
