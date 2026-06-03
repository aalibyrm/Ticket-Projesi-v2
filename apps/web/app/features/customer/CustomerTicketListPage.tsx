import AddOutlinedIcon from "@mui/icons-material/AddOutlined";
import {
  Box,
  Button,
  Paper,
  Stack,
  Tab,
  Tabs,
  Typography,
} from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router";
import {
  CustomerEmptyState,
  CustomerErrorState,
  CustomerLoadingState,
} from "~/features/customer/components/CustomerState";
import { PriorityChip, TicketStatusChip } from "~/features/customer/components/StatusChips";
import { formatDate } from "~/features/customer/formatters";
import { useCustomerTickets } from "~/features/customer/customerQueries";
import type { TicketResponse } from "~/features/customer/customerTypes";

type TicketFilter = "ALL" | "OPEN" | "CLOSED";

const columns: GridColDef<TicketResponse>[] = [
  { field: "ticketNumber", flex: 1, headerName: "ID", minWidth: 150 },
  { field: "summary", flex: 2, headerName: "Konu", minWidth: 240 },
  { field: "productName", flex: 1.2, headerName: "Kategori", minWidth: 160 },
  {
    field: "priority",
    headerName: "Oncelik",
    minWidth: 130,
    renderCell: ({ row }) => <PriorityChip priority={row.priority} />,
  },
  {
    field: "updatedAt",
    headerName: "Tarih",
    minWidth: 160,
    valueFormatter: (value: string) => formatDate(value),
  },
  {
    field: "status",
    headerName: "Durum",
    minWidth: 170,
    renderCell: ({ row }) => <TicketStatusChip status={row.status} />,
  },
];

export function CustomerTicketListPage() {
  const navigate = useNavigate();
  const [filter, setFilter] = useState<TicketFilter>("ALL");
  const { data, isError, isLoading, refetch } = useCustomerTickets();

  const rows = useMemo(() => {
    const tickets = data ?? [];
    if (filter === "OPEN") {
      return tickets.filter((ticket) => ticket.status !== "RESOLVED" && ticket.status !== "CLOSED");
    }
    if (filter === "CLOSED") {
      return tickets.filter((ticket) => ticket.status === "RESOLVED" || ticket.status === "CLOSED");
    }
    return tickets;
  }, [data, filter]);

  if (isLoading) {
    return <CustomerLoadingState label="Ticket listesi yukleniyor" />;
  }

  if (isError) {
    return <CustomerErrorState onRetry={() => void refetch()} />;
  }

  return (
    <Stack spacing={3}>
      <Stack
        alignItems={{ md: "center", xs: "flex-start" }}
        direction={{ md: "row", xs: "column" }}
        justifyContent="space-between"
        spacing={2}
      >
        <Stack spacing={0.75}>
          <Typography variant="overline">Musteri portali</Typography>
          <Typography variant="h4">Taleplerim</Typography>
        </Stack>
        <Button
          component={Link}
          startIcon={<AddOutlinedIcon />}
          to="/tickets/new"
          variant="contained"
        >
          Yeni talep
        </Button>
      </Stack>

      <Tabs
        onChange={(_, value: TicketFilter) => setFilter(value)}
        value={filter}
        sx={{ borderBottom: "1px solid", borderColor: "divider", minHeight: 40 }}
      >
        <Tab label="Tumu" value="ALL" />
        <Tab label="Acik" value="OPEN" />
        <Tab label="Kapali" value="CLOSED" />
      </Tabs>

      {rows.length === 0 ? (
        <CustomerEmptyState message="Bu filtrede ticket bulunmuyor." />
      ) : (
        <Paper sx={{ p: 2 }}>
          <Box sx={{ height: 520, width: "100%" }}>
            <DataGrid
              columns={columns}
              disableRowSelectionOnClick
              getRowId={(row) => row.id}
              initialState={{
                pagination: {
                  paginationModel: { pageSize: 10 },
                },
              }}
              onRowClick={({ row }) => navigate(`/tickets/${row.id}`)}
              pageSizeOptions={[10, 25]}
              rows={rows}
              sx={{
                "& .MuiDataGrid-row": {
                  cursor: "pointer",
                },
              }}
            />
          </Box>
        </Paper>
      )}
    </Stack>
  );
}
