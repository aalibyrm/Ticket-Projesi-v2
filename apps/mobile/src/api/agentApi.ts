import { apiClient } from "./httpClient";
import type {
  AssignTicketRequest,
  ChangeTicketStatusRequest,
  TicketCommentResponse,
  TicketResponse
} from "./mobileApiTypes";

export async function listAgentTickets() {
  const response = await apiClient.get<TicketResponse[]>("/api/agent/tickets");
  return response.data;
}

export async function getAgentTicket(ticketId: string) {
  const response = await apiClient.get<TicketResponse>(`/api/agent/tickets/${ticketId}`);
  return response.data;
}

export async function listAgentTicketComments(ticketId: string) {
  const response = await apiClient.get<TicketCommentResponse[]>(`/api/agent/tickets/${ticketId}/comments`);
  return response.data;
}

export async function addAgentExternalComment(ticketId: string, body: string) {
  const response = await apiClient.post<TicketCommentResponse>(`/api/agent/tickets/${ticketId}/comments/external`, {
    body
  });
  return response.data;
}

export async function assignAgentTicket(ticketId: string, request: AssignTicketRequest) {
  const response = await apiClient.patch<TicketResponse>(`/api/agent/tickets/${ticketId}/assignment`, request);
  return response.data;
}

export async function changeAgentTicketStatus(ticketId: string, request: ChangeTicketStatusRequest) {
  const response = await apiClient.patch<TicketResponse>(`/api/agent/tickets/${ticketId}/status`, request);
  return response.data;
}
