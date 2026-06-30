import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  addAgentExternalComment,
  addAgentInternalNote,
  addAgentWorklog,
  assignAgentTicket,
  changeAgentTicketStatus,
  createAgentAttachmentDownloadUrl,
  getAgentTicket,
  getAgentTicketConversationReadState,
  listAgentTicketComments,
  listAgentTickets,
  listAgentWorklogs,
  listSupportTeamMembers,
  listSupportTeams,
  markAgentTicketConversationRead,
} from "~/features/agent/agentApi";
import type {
  AddWorklogRequest,
  AssignTicketRequest,
  ChangeTicketStatusRequest,
} from "~/features/agent/agentTypes";

export const agentQueryKeys = {
  comments: (ticketId: string) => ["agent", "ticket", ticketId, "comments"] as const,
  readState: (ticketId: string) => ["agent", "ticket", ticketId, "comments", "read-state"] as const,
  teamMembers: (teamId: string) => ["agent", "organization", "teams", teamId, "members"] as const,
  teams: ["agent", "organization", "teams"] as const,
  ticket: (ticketId: string) => ["agent", "ticket", ticketId] as const,
  tickets: ["agent", "tickets"] as const,
  worklogs: (ticketId: string) => ["agent", "ticket", ticketId, "worklogs"] as const,
};

export function useAgentTickets() {
  return useQuery({
    queryFn: listAgentTickets,
    queryKey: agentQueryKeys.tickets,
  });
}

export function useAgentTicket(ticketId: string) {
  return useQuery({
    enabled: Boolean(ticketId),
    queryFn: () => getAgentTicket(ticketId),
    queryKey: agentQueryKeys.ticket(ticketId),
  });
}

export function useSupportTeams() {
  return useQuery({
    queryFn: listSupportTeams,
    queryKey: agentQueryKeys.teams,
    staleTime: 300_000,
  });
}

export function useSupportTeamMembers(teamId: string) {
  return useQuery({
    enabled: Boolean(teamId),
    queryFn: () => listSupportTeamMembers(teamId),
    queryKey: agentQueryKeys.teamMembers(teamId),
    staleTime: 300_000,
  });
}

export function useAgentTicketComments(ticketId: string) {
  return useQuery({
    enabled: Boolean(ticketId),
    queryFn: () => listAgentTicketComments(ticketId),
    queryKey: agentQueryKeys.comments(ticketId),
  });
}

export function useAgentTicketConversationReadState(ticketId: string) {
  return useQuery({
    enabled: Boolean(ticketId),
    queryFn: () => getAgentTicketConversationReadState(ticketId),
    queryKey: agentQueryKeys.readState(ticketId),
  });
}

export function useAgentWorklogs(ticketId: string, enabled = true) {
  return useQuery({
    enabled: Boolean(ticketId) && enabled,
    queryFn: () => listAgentWorklogs(ticketId),
    queryKey: agentQueryKeys.worklogs(ticketId),
  });
}

export function useAddAgentExternalComment(ticketId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (body: string) => addAgentExternalComment(ticketId, body),
    onSuccess: () => invalidateTicketWorkspace(queryClient, ticketId),
  });
}

export function useAddAgentInternalNote(ticketId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (body: string) => addAgentInternalNote(ticketId, body),
    onSuccess: () => invalidateTicketWorkspace(queryClient, ticketId),
  });
}

export function useAddAgentWorklog(ticketId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: AddWorklogRequest) => addAgentWorklog(ticketId, request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: agentQueryKeys.worklogs(ticketId) });
      void queryClient.invalidateQueries({ queryKey: agentQueryKeys.tickets });
    },
  });
}

export function useChangeAgentTicketStatus(ticketId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: ChangeTicketStatusRequest) => changeAgentTicketStatus(ticketId, request),
    onSuccess: () => invalidateTicketWorkspace(queryClient, ticketId),
  });
}

export function useAssignAgentTicket(ticketId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: AssignTicketRequest) => assignAgentTicket(ticketId, request),
    onSuccess: () => invalidateTicketWorkspace(queryClient, ticketId),
  });
}

export function useAgentAttachmentDownloadUrl() {
  return useMutation({
    mutationFn: (fileId: string) => createAgentAttachmentDownloadUrl(fileId),
  });
}

export function useMarkAgentTicketConversationRead(ticketId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => markAgentTicketConversationRead(ticketId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: agentQueryKeys.readState(ticketId) });
      void queryClient.invalidateQueries({ queryKey: agentQueryKeys.tickets });
    },
  });
}

function invalidateTicketWorkspace(
  queryClient: ReturnType<typeof useQueryClient>,
  ticketId: string,
) {
  void queryClient.invalidateQueries({ queryKey: agentQueryKeys.comments(ticketId) });
  void queryClient.invalidateQueries({ queryKey: agentQueryKeys.readState(ticketId) });
  void queryClient.invalidateQueries({ queryKey: agentQueryKeys.ticket(ticketId) });
  void queryClient.invalidateQueries({ queryKey: agentQueryKeys.tickets });
}
