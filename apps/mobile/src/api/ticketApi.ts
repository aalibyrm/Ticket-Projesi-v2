import { apiClient } from "./httpClient";
import type {
  CreateTicketRequest,
  ProductResponse,
  TicketCommentResponse,
  TicketResponse,
  TicketTopicResponse
} from "./mobileApiTypes";

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
    body
  });
  return response.data;
}
