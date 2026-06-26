import AssignmentTurnedInOutlinedIcon from "@mui/icons-material/AssignmentTurnedInOutlined";
import ReportProblemOutlinedIcon from "@mui/icons-material/ReportProblemOutlined";
import SupportAgentOutlinedIcon from "@mui/icons-material/SupportAgentOutlined";
import {
  Box,
  Chip,
  Divider,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import { useMemo } from "react";
import { useParams } from "react-router";
import { AgentErrorState, AgentLoadingState } from "~/features/agent/components/AgentState";
import { AgentTicketActionPanel } from "~/features/agent/components/AgentTicketActionPanel";
import { AgentTicketConversation } from "~/features/agent/components/AgentTicketConversation";
import { AgentTicketQueue } from "~/features/agent/components/AgentTicketQueue";
import { useAgentTicket, useAgentTickets } from "~/features/agent/agentQueries";
import type { TicketResponse } from "~/features/agent/agentTypes";
import { PriorityChip, TicketStatusChip } from "~/features/customer/components/StatusChips";
import { formatDateTime } from "~/features/customer/formatters";
import { actorDisplayName } from "~/shared/userDisplay";

export function AgentWorkbenchPage() {
  const { ticketId } = useParams();
  const ticketsQuery = useAgentTickets();

  if (ticketsQuery.isLoading) {
    return <AgentLoadingState label="Agent kuyrugu yukleniyor" />;
  }

  if (ticketsQuery.isError || !ticketsQuery.data) {
    return <AgentErrorState onRetry={() => void ticketsQuery.refetch()} />;
  }

  return (
    <Stack spacing={2} sx={{ minHeight: 0 }}>
      <Stack spacing={0.5}>
        <Typography variant="overline">Temsilci paneli</Typography>
        <Typography variant="h4">Is kuyrugu</Typography>
      </Stack>
      <Paper
        sx={{
          display: "flex",
          height: "calc(100vh - 184px)",
          minHeight: 560,
          overflow: "hidden",
        }}
        variant="outlined"
      >
        <AgentTicketQueue selectedTicketId={ticketId} tickets={ticketsQuery.data} />
        {ticketId ? (
          <AgentTicketDetailWorkspace ticketId={ticketId} />
        ) : (
          <AgentInboxSummary tickets={ticketsQuery.data} />
        )}
      </Paper>
    </Stack>
  );
}

function AgentTicketDetailWorkspace({ ticketId }: { ticketId: string }) {
  const ticketQuery = useAgentTicket(ticketId);

  if (ticketQuery.isLoading) {
    return (
      <Box sx={{ flex: 1 }}>
        <AgentLoadingState label="Ticket detayi yukleniyor" />
      </Box>
    );
  }

  if (ticketQuery.isError || !ticketQuery.data) {
    return (
      <Box sx={{ flex: 1 }}>
        <AgentErrorState onRetry={() => void ticketQuery.refetch()} />
      </Box>
    );
  }

  const ticket = ticketQuery.data;

  return (
    <Box sx={{ display: "flex", flex: 1, minHeight: 0, minWidth: 0 }}>
      <Stack sx={{ flex: 1, minHeight: 0, minWidth: 0 }}>
        <Stack
          direction={{ md: "row", xs: "column" }}
          justifyContent="space-between"
          spacing={2}
          sx={{ bgcolor: "background.paper", borderBottom: "1px solid", borderColor: "divider", flexShrink: 0, p: 2.5 }}
        >
          <Stack spacing={1}>
            <Stack alignItems="center" direction="row" spacing={1}>
              <Typography color="text.secondary" variant="body2">
                {ticket.ticketNumber}
              </Typography>
              <Divider flexItem orientation="vertical" />
              <Typography color="text.secondary" variant="body2">
                {ticket.productName}
              </Typography>
            </Stack>
            <Typography variant="h5">{ticket.summary}</Typography>
            <Stack direction="row" spacing={1}>
              <TicketStatusChip status={ticket.status} />
              <PriorityChip priority={ticket.priority} />
            </Stack>
          </Stack>
          <Stack alignItems={{ md: "flex-end", xs: "flex-start" }} spacing={0.5}>
            <Typography color="text.secondary" variant="body2">
              Guncelleme: {formatDateTime(ticket.updatedAt)}
            </Typography>
            <Typography color="text.secondary" variant="body2">
              Musteri: {actorDisplayName(ticket.customerId, undefined, "Musteri")}
            </Typography>
          </Stack>
        </Stack>
        <AgentTicketConversation ticket={ticket} />
      </Stack>
      <AgentTicketActionPanel ticket={ticket} />
    </Box>
  );
}

function AgentInboxSummary({ tickets }: { tickets: TicketResponse[] }) {
  const metrics = useMemo(() => {
    const open = tickets.filter((ticket) => ticket.status !== "RESOLVED" && ticket.status !== "CLOSED").length;
    const high = tickets.filter((ticket) => ticket.priority === "HIGH" && ticket.status !== "CLOSED").length;
    const waiting = tickets.filter((ticket) => ticket.status === "WAITING_FOR_CUSTOMER").length;
    return [
      { icon: <AssignmentTurnedInOutlinedIcon />, label: "Aktif bilet", value: open },
      { icon: <ReportProblemOutlinedIcon />, label: "Yuksek oncelik", value: high },
      { icon: <SupportAgentOutlinedIcon />, label: "Musteri bekleyen", value: waiting },
    ];
  }, [tickets]);

  return (
    <Stack spacing={3} sx={{ flex: 1, minHeight: 0, overflowY: "auto", p: 3 }}>
      <Stack direction={{ md: "row", xs: "column" }} spacing={2}>
        {metrics.map((item) => (
          <Paper key={item.label} sx={{ flex: 1, p: 2 }} variant="outlined">
            <Stack spacing={2}>
              <Stack alignItems="center" direction="row" spacing={1}>
                {item.icon}
                <Typography color="text.secondary">{item.label}</Typography>
              </Stack>
              <Typography variant="h3">{item.value}</Typography>
            </Stack>
          </Paper>
        ))}
      </Stack>
      <Paper sx={{ p: 3 }} variant="outlined">
        <Stack spacing={2}>
          <Typography variant="h6">Son guncellenenler</Typography>
          {tickets.slice(0, 5).map((ticket) => (
            <Stack
              alignItems="center"
              direction={{ md: "row", xs: "column" }}
              justifyContent="space-between"
              key={ticket.id}
              spacing={1}
            >
              <Stack spacing={0.5}>
                <Typography variant="subtitle2">{ticket.summary}</Typography>
                <Typography color="text.secondary" variant="body2">
                  {ticket.ticketNumber} / {formatDateTime(ticket.updatedAt)}
                </Typography>
              </Stack>
              <Stack direction="row" spacing={1}>
                <Chip label={ticket.productCode} size="small" />
                <PriorityChip priority={ticket.priority} />
              </Stack>
            </Stack>
          ))}
          {tickets.length === 0 && (
            <Typography color="text.secondary">Atanmis bilet yok.</Typography>
          )}
        </Stack>
      </Paper>
    </Stack>
  );
}
