import {
  Stack,
  Typography,
} from "@mui/material";
import axios from "axios";
import { useMemo, useState } from "react";
import { useNavigate } from "react-router";
import {
  CustomerEmptyState,
  CustomerErrorState,
  CustomerLoadingState,
} from "~/features/customer/components/CustomerState";
import { TicketStatusChip } from "~/features/customer/components/StatusChips";
import { formatDate } from "~/features/customer/formatters";
import { useCustomerTickets } from "~/features/customer/customerQueries";
import type { TicketResponse } from "~/features/customer/customerTypes";
import {
  TmButton,
  TmDataTable,
  type TmDataTableColumn,
  TmFilterTabs,
  TmSurface,
} from "~/shared/design-system";
import { tmTokens } from "~/shared/theme/tmTokens";

type TicketFilter = "ALL" | "OPEN" | "CLOSED";

const filterItems = [
  { label: "Tumu", value: "ALL" },
  { label: "Acik", value: "OPEN" },
  { label: "Kapali", value: "CLOSED" },
] satisfies Array<{ label: string; value: TicketFilter }>;

const columns: TmDataTableColumn<TicketResponse>[] = [
  {
    header: "ID",
    id: "ticketNumber",
    render: (row) => (
      <Typography color="text.secondary" sx={tmTokens.typography.bodyMd}>
        {row.ticketNumber}
      </Typography>
    ),
    width: "minmax(132px, 0.9fr)",
  },
  {
    header: "Konu",
    id: "summary",
    render: (row) => (
      <Typography
        sx={{
          ...tmTokens.typography.headlineSm,
          color: tmTokens.colors.onSurface,
          overflow: "hidden",
          textOverflow: "ellipsis",
          whiteSpace: "nowrap",
        }}
      >
        {row.summary}
      </Typography>
    ),
    width: "minmax(260px, 3fr)",
  },
  {
    header: "Tarih",
    id: "updatedAt",
    render: (row) => (
      <Typography color="text.secondary" sx={tmTokens.typography.bodyMd}>
        {formatDate(row.updatedAt)}
      </Typography>
    ),
    width: "minmax(140px, 1fr)",
  },
  {
    header: "Durum",
    id: "status",
    render: (row) => <TicketStatusChip status={row.status} />,
    width: "minmax(132px, 0.9fr)",
  },
];

export function CustomerTicketListPage() {
  const navigate = useNavigate();
  const [filter, setFilter] = useState<TicketFilter>("ALL");
  const { data, error, isError, isLoading, refetch } = useCustomerTickets();

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
    return <CustomerErrorState message={customerTicketErrorMessage(error)} onRetry={() => void refetch()} />;
  }

  return (
    <Stack spacing={3}>
      <Stack spacing={1.5}>
        <Typography component="h1" sx={tmTokens.typography.headlineXl}>
          Taleplerim
        </Typography>
        <TmFilterTabs items={filterItems} onChange={setFilter} value={filter} />
      </Stack>

      {rows.length === 0 ? (
        <CustomerEmptyState message="Bu filtrede ticket bulunmuyor." />
      ) : (
        <TmSurface>
          <TmDataTable
            columns={columns}
            getRowId={(row) => row.id}
            onRowClick={(row) => navigate(`/tickets/${row.id}`)}
            rowAriaLabel={(row) => `${row.ticketNumber} ticket detayini ac`}
            rows={rows}
          />
        </TmSurface>
      )}

      <Stack alignItems="center" direction="row" justifyContent="space-between">
        <TmButton disabled variant="outlined">
          Onceki
        </TmButton>
        <Typography color="text.secondary" sx={tmTokens.typography.bodyMd}>
          Sayfa 1 / 1
        </Typography>
        <TmButton disabled={rows.length <= 10} variant="outlined">
          Sonraki
        </TmButton>
      </Stack>
    </Stack>
  );
}

function customerTicketErrorMessage(error: unknown) {
  if (!axios.isAxiosError(error)) {
    return "Veri alinirken hata olustu.";
  }

  if (!error.response) {
    return "API Gateway'e ulasilamadi. Gateway ve CORS ayarlarini kontrol edin.";
  }

  if (error.response.status === 401) {
    return "Oturum dogrulanamadi. Cikis yapip tekrar giris yapin.";
  }

  if (error.response.status === 403) {
    return "Bu ticket listesi icin yetkiniz yok.";
  }

  return `Veri alinirken hata olustu. HTTP ${error.response.status}`;
}
