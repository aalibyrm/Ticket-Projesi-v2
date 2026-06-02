import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  addCustomerTicketComment,
  createAttachmentDownloadUrl,
  createCustomerTicket,
  getCustomerTicket,
  listCustomerTicketComments,
  listCustomerTickets,
  listNotifications,
  listProducts,
  listTicketTopics,
  markNotificationRead,
  uploadTicketAttachment,
} from "~/features/customer/customerApi";
import type { CreateTicketRequest } from "~/features/customer/customerTypes";

export const customerQueryKeys = {
  comments: (ticketId: string) => ["customer", "ticket", ticketId, "comments"] as const,
  notifications: (read?: boolean) => ["customer", "notifications", read ?? "all"] as const,
  products: ["customer", "products"] as const,
  ticket: (ticketId: string) => ["customer", "ticket", ticketId] as const,
  tickets: ["customer", "tickets"] as const,
  topics: ["customer", "ticket-topics"] as const,
};

export function useCustomerTickets() {
  return useQuery({
    queryFn: listCustomerTickets,
    queryKey: customerQueryKeys.tickets,
  });
}

export function useCustomerTicket(ticketId: string) {
  return useQuery({
    enabled: Boolean(ticketId),
    queryFn: () => getCustomerTicket(ticketId),
    queryKey: customerQueryKeys.ticket(ticketId),
  });
}

export function useProducts() {
  return useQuery({
    queryFn: listProducts,
    queryKey: customerQueryKeys.products,
    staleTime: 300_000,
  });
}

export function useTicketTopics() {
  return useQuery({
    queryFn: listTicketTopics,
    queryKey: customerQueryKeys.topics,
    staleTime: 300_000,
  });
}

export function useCustomerTicketComments(ticketId: string) {
  return useQuery({
    enabled: Boolean(ticketId),
    queryFn: () => listCustomerTicketComments(ticketId),
    queryKey: customerQueryKeys.comments(ticketId),
  });
}

export function useCreateCustomerTicket() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: CreateTicketRequest) => createCustomerTicket(request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: customerQueryKeys.tickets }),
  });
}

export function useAddCustomerTicketComment(ticketId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (body: string) => addCustomerTicketComment(ticketId, body),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: customerQueryKeys.comments(ticketId) });
      void queryClient.invalidateQueries({ queryKey: customerQueryKeys.ticket(ticketId) });
    },
  });
}

export function useUploadTicketAttachment() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ file, ticketId }: { file: File; ticketId: string }) => uploadTicketAttachment(ticketId, file),
    onSuccess: (_, variables) => {
      void queryClient.invalidateQueries({ queryKey: customerQueryKeys.ticket(variables.ticketId) });
      void queryClient.invalidateQueries({ queryKey: customerQueryKeys.tickets });
    },
  });
}

export function useAttachmentDownloadUrl() {
  return useMutation({
    mutationFn: (fileId: string) => createAttachmentDownloadUrl(fileId),
  });
}

export function useNotifications(read?: boolean) {
  return useQuery({
    queryFn: () => listNotifications(read),
    queryKey: customerQueryKeys.notifications(read),
  });
}

export function useMarkNotificationRead() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (notificationId: string) => markNotificationRead(notificationId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: customerQueryKeys.notifications() });
      void queryClient.invalidateQueries({ queryKey: customerQueryKeys.notifications(false) });
      void queryClient.invalidateQueries({ queryKey: customerQueryKeys.notifications(true) });
    },
  });
}
