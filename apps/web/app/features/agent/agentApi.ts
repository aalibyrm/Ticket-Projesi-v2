import {
  createAttachmentDownloadUrl,
} from "~/features/customer/customerApi";
import type {
  AddWorklogRequest,
  AssignTicketRequest,
  ChangeTicketStatusRequest,
  DownloadUrlResponse,
  SupportTeamResponse,
  TeamMemberResponse,
  TicketCommentResponse,
  TicketResponse,
  TicketWorklogResponse,
} from "~/features/agent/agentTypes";
import { apiClient } from "~/shared/api/httpClient";

export async function listAgentTickets() {
  const response = await apiClient.get<TicketResponse[]>("/api/agent/tickets");
  return response.data;
}

export async function listSupportTeams() {
  const response = await apiClient.get<SupportTeamResponse[]>("/api/organization/teams");
  return response.data;
}

export async function listSupportTeamMembers(teamId: string) {
  const response = await apiClient.get<TeamMemberResponse[]>(`/api/organization/teams/${teamId}/members`);
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
    body,
  });
  return response.data;
}

export async function addAgentInternalNote(ticketId: string, body: string) {
  const response = await apiClient.post<TicketCommentResponse>(`/api/agent/tickets/${ticketId}/comments/internal`, {
    body,
  });
  return response.data;
}

export async function listAgentWorklogs(ticketId: string) {
  const response = await apiClient.get<TicketWorklogResponse[]>(`/api/agent/tickets/${ticketId}/worklogs`);
  return response.data;
}

export async function addAgentWorklog(ticketId: string, request: AddWorklogRequest) {
  const response = await apiClient.post<TicketWorklogResponse>(`/api/agent/tickets/${ticketId}/worklogs`, request);
  return response.data;
}

export async function changeAgentTicketStatus(ticketId: string, request: ChangeTicketStatusRequest) {
  const response = await apiClient.patch<TicketResponse>(`/api/agent/tickets/${ticketId}/status`, request);
  return response.data;
}

export async function assignAgentTicket(ticketId: string, request: AssignTicketRequest) {
  const response = await apiClient.patch<TicketResponse>(`/api/agent/tickets/${ticketId}/assignment`, request);
  return response.data;
}

export async function createAgentAttachmentDownloadUrl(fileId: string): Promise<DownloadUrlResponse> {
  return createAttachmentDownloadUrl(fileId);
}
