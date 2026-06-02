import SearchOutlinedIcon from "@mui/icons-material/SearchOutlined";
import {
  Box,
  ButtonBase,
  Chip,
  Divider,
  InputAdornment,
  Stack,
  TextField,
  ToggleButton,
  ToggleButtonGroup,
  Typography,
} from "@mui/material";
import { useMemo, useState } from "react";
import { useNavigate } from "react-router";
import { PriorityChip, TicketStatusChip } from "~/features/customer/components/StatusChips";
import { formatDateTime } from "~/features/customer/formatters";
import type {
  AgentTicketFilters,
  TicketPriority,
  TicketResponse,
} from "~/features/agent/agentTypes";

const defaultFilters: AgentTicketFilters = {
  priority: "ALL",
  search: "",
  status: "OPEN",
};

export function AgentTicketQueue({
  selectedTicketId,
  tickets,
}: {
  selectedTicketId?: string;
  tickets: TicketResponse[];
}) {
  const navigate = useNavigate();
  const [filters, setFilters] = useState(defaultFilters);
  const visibleTickets = useMemo(() => filterTickets(tickets, filters), [filters, tickets]);
  const openCount = tickets.filter((ticket) => ticket.status !== "RESOLVED" && ticket.status !== "CLOSED").length;

  return (
    <Stack
      sx={{
        bgcolor: "background.paper",
        borderRight: "1px solid",
        borderColor: "divider",
        flex: "0 0 320px",
        minHeight: "calc(100vh - 144px)",
      }}
    >
      <Stack spacing={2} sx={{ p: 2.5 }}>
        <Stack direction="row" justifyContent="space-between" spacing={1}>
          <Typography variant="h6">Atanan Biletler</Typography>
          <Chip label={`${openCount} acik`} size="small" />
        </Stack>
        <TextField
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchOutlinedIcon fontSize="small" />
              </InputAdornment>
            ),
          }}
          onChange={(event) => setFilters((current) => ({ ...current, search: event.target.value }))}
          placeholder="Bilet ara"
          size="small"
          value={filters.search}
          variant="standard"
        />
        <ToggleButtonGroup
          exclusive
          fullWidth
          onChange={(_, value: AgentTicketFilters["status"] | null) =>
            value && setFilters((current) => ({ ...current, status: value }))
          }
          size="small"
          value={filters.status}
        >
          <ToggleButton value="OPEN">Acik</ToggleButton>
          <ToggleButton value="ALL">Tumu</ToggleButton>
          <ToggleButton value="CLOSED">Kapali</ToggleButton>
        </ToggleButtonGroup>
        <ToggleButtonGroup
          exclusive
          fullWidth
          onChange={(_, value: AgentTicketFilters["priority"] | null) =>
            value && setFilters((current) => ({ ...current, priority: value }))
          }
          size="small"
          value={filters.priority}
        >
          <ToggleButton value="ALL">Tumu</ToggleButton>
          <ToggleButton value="HIGH">Yuksek</ToggleButton>
          <ToggleButton value="MEDIUM">Normal</ToggleButton>
          <ToggleButton value="LOW">Dusuk</ToggleButton>
        </ToggleButtonGroup>
      </Stack>
      <Divider />
      <Stack sx={{ flex: 1, overflowY: "auto" }}>
        {visibleTickets.length === 0 ? (
          <Typography color="text.secondary" sx={{ p: 2.5 }}>
            Uygun bilet bulunamadi.
          </Typography>
        ) : (
          visibleTickets.map((ticket) => (
            <ButtonBase
              key={ticket.id}
              onClick={() => navigate(`/agent/tickets/${ticket.id}`)}
              sx={{
                alignItems: "stretch",
                borderBottom: "1px solid",
                borderColor: "divider",
                borderLeft: "4px solid",
                borderLeftColor: selectedTicketId === ticket.id ? "primary.main" : "transparent",
                display: "block",
                p: 0,
                textAlign: "left",
                width: "100%",
                "&:hover": {
                  bgcolor: "action.hover",
                },
              }}
            >
              <Stack spacing={1} sx={{ p: 2 }}>
                <Stack direction="row" justifyContent="space-between" spacing={1}>
                  <Typography color="text.secondary" variant="caption">
                    {ticket.ticketNumber}
                  </Typography>
                  <Typography color="text.secondary" variant="caption">
                    {formatDateTime(ticket.updatedAt)}
                  </Typography>
                </Stack>
                <Typography noWrap variant="subtitle2">
                  {ticket.summary}
                </Typography>
                <Typography color="text.secondary" sx={{ minHeight: 40 }} variant="body2">
                  {ticket.description.slice(0, 120)}
                </Typography>
                <Box sx={{ display: "flex", flexWrap: "wrap", gap: 1 }}>
                  <TicketStatusChip status={ticket.status} />
                  <PriorityChip priority={ticket.priority} />
                </Box>
              </Stack>
            </ButtonBase>
          ))
        )}
      </Stack>
    </Stack>
  );
}

function filterTickets(tickets: TicketResponse[], filters: AgentTicketFilters) {
  const query = filters.search.trim().toLocaleLowerCase("tr-TR");
  return tickets.filter((ticket) => {
    const matchesSearch = !query
      || ticket.ticketNumber.toLocaleLowerCase("tr-TR").includes(query)
      || ticket.summary.toLocaleLowerCase("tr-TR").includes(query)
      || ticket.description.toLocaleLowerCase("tr-TR").includes(query);
    const matchesPriority = filters.priority === "ALL" || ticket.priority === filters.priority;
    const matchesStatus = matchesStatusFilter(ticket.status, filters.status);
    return matchesSearch && matchesPriority && matchesStatus;
  });
}

function matchesStatusFilter(status: string, filter: AgentTicketFilters["status"]) {
  if (filter === "ALL") {
    return true;
  }
  if (filter === "OPEN") {
    return status !== "RESOLVED" && status !== "CLOSED";
  }
  return status === "RESOLVED" || status === "CLOSED";
}
