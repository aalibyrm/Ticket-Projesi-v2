import type {
  CreateTicketRequest,
  ConversationReadStateResponse,
  DownloadUrlResponse,
  NotificationResponse,
  ProductResponse,
  TicketCommentResponse,
  TicketTopicResponse,
  TicketResponse,
  UploadUrlResponse,
} from "~/features/customer/customerTypes";
import { apiClient } from "~/shared/api/httpClient";

export async function listCustomerTickets() {
  const response = await apiClient.get<TicketResponse[]>("/api/tickets");
  return response.data;
}

export async function getCustomerTicket(ticketId: string) {
  const response = await apiClient.get<TicketResponse>(`/api/tickets/${ticketId}`);
  return response.data;
}

export async function createCustomerTicket(request: CreateTicketRequest) {
  const response = await apiClient.post<TicketResponse>("/api/tickets", request);
  return response.data;
}

export async function listProducts() {
  const response = await apiClient.get<ProductResponse[]>("/api/products");
  return response.data;
}

export async function listTicketTopics() {
  const response = await apiClient.get<TicketTopicResponse[]>("/api/ticket-topics");
  return response.data;
}

export async function listCustomerTicketComments(ticketId: string) {
  const response = await apiClient.get<TicketCommentResponse[]>(`/api/tickets/${ticketId}/comments`);
  return response.data;
}

export async function addCustomerTicketComment(ticketId: string, body: string) {
  const response = await apiClient.post<TicketCommentResponse>(`/api/tickets/${ticketId}/comments/external`, {
    body,
  });
  return response.data;
}

export async function getCustomerTicketConversationReadState(ticketId: string) {
  const response = await apiClient.get<ConversationReadStateResponse>(`/api/tickets/${ticketId}/comments/read-state`);
  return response.data;
}

export async function markCustomerTicketConversationRead(ticketId: string) {
  const response = await apiClient.post<ConversationReadStateResponse>(`/api/tickets/${ticketId}/comments/read`);
  return response.data;
}

export async function uploadTicketAttachment(ticketId: string, file: File) {
  const uploadUrl = await createUploadUrl(ticketId, file);
  const uploadHeaders = new Headers(uploadUrl.requiredHeaders);

  if (!uploadHeaders.has("Content-Type")) {
    uploadHeaders.set("Content-Type", file.type || "application/octet-stream");
  }

  const uploadResponse = await fetch(uploadUrl.uploadUrl, {
    body: file,
    headers: uploadHeaders,
    method: uploadUrl.method,
  });

  if (!uploadResponse.ok) {
    throw new Error("Dosya object storage alanina yuklenemedi.");
  }

  await apiClient.post(`/api/files/uploads/${uploadUrl.fileId}/complete`);
  return uploadUrl.fileId;
}

export async function createAttachmentDownloadUrl(fileId: string) {
  const response = await apiClient.post<DownloadUrlResponse>(`/api/files/${fileId}/download-url`);
  return response.data;
}

export async function listNotifications(read?: boolean) {
  const response = await apiClient.get<NotificationResponse[]>("/api/notifications", {
    params: read === undefined ? undefined : { read },
  });
  return response.data;
}

export async function markNotificationRead(notificationId: string) {
  const response = await apiClient.patch<NotificationResponse>(`/api/notifications/${notificationId}/read`);
  return response.data;
}

async function createUploadUrl(ticketId: string, file: File) {
  const response = await apiClient.post<UploadUrlResponse>("/api/files/uploads", {
    contentType: file.type || "application/octet-stream",
    originalFilename: file.name,
    sizeBytes: file.size,
    ticketId,
  });

  return response.data;
}
