import AssessmentOutlinedIcon from "@mui/icons-material/AssessmentOutlined";
import GroupsOutlinedIcon from "@mui/icons-material/GroupsOutlined";
import RefreshOutlinedIcon from "@mui/icons-material/RefreshOutlined";
import ReportProblemOutlinedIcon from "@mui/icons-material/ReportProblemOutlined";
import TimelapseOutlinedIcon from "@mui/icons-material/TimelapseOutlined";
import {
  Box,
  Button,
  Chip,
  Divider,
  LinearProgress,
  Paper,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import type { ReactNode } from "react";
import { useMemo, useState } from "react";
import { formatDate, formatDateTime } from "~/features/customer/formatters";
import { ReportErrorState, ReportLoadingState } from "~/features/reports/components/ReportState";
import {
  useAgentPerformanceReport,
  useClosedTicketReport,
  useSlaComplianceReport,
  useTicketStatusDistribution,
} from "~/features/reports/reportQueries";
import type {
  AgentPerformanceRowResponse,
  ClosedTicketDateRangeResponse,
  ReportDateRange,
  ReportNumericValue,
  SlaComplianceReportResponse,
  TicketStatusDistributionResponse,
} from "~/features/reports/reportTypes";

type ProgressColor = "error" | "primary" | "success" | "warning";

const agentColumns: GridColDef<AgentPerformanceRowResponse>[] = [
  {
    field: "agentId",
    flex: 1.4,
    headerName: "Agent",
    minWidth: 180,
    valueFormatter: (value: string) => shortId(value),
  },
  {
    field: "assignedTicketCount",
    headerName: "Atanmis",
    minWidth: 120,
    type: "number",
  },
  {
    field: "resolvedTicketCount",
    headerName: "Cozulen",
    minWidth: 120,
    type: "number",
  },
  {
    field: "totalWorklogMinutes",
    headerName: "Worklog",
    minWidth: 140,
    type: "number",
    valueFormatter: (value: number) => formatMinutes(value),
  },
  {
    field: "averageResolutionMinutes",
    flex: 1,
    headerName: "Ort. cozum",
    minWidth: 150,
    valueFormatter: (value: ReportNumericValue) => formatMinutes(value),
  },
];

const statusLabels: Record<string, string> = {
  CLOSED: "Kapali",
  IN_PROGRESS: "Devam ediyor",
  NEW: "Yeni",
  RESOLVED: "Cozuldu",
  WAITING_FOR_CUSTOMER: "Musteri bekliyor",
};

const priorityLabels: Record<string, string> = {
  CRITICAL: "Kritik",
  HIGH: "Yuksek",
  LOW: "Dusuk",
  MEDIUM: "Orta",
};

const defaultRange = createDefaultDateRange();

export function ManagerReportsPage() {
  const [dateRange, setDateRange] = useState<ReportDateRange>(defaultRange);
  const statusQuery = useTicketStatusDistribution();
  const closedQuery = useClosedTicketReport(dateRange);
  const agentQuery = useAgentPerformanceReport();
  const slaQuery = useSlaComplianceReport();

  const queries = [statusQuery, closedQuery, agentQuery, slaQuery];
  const isLoading = queries.some((query) => query.isLoading);
  const isFetching = queries.some((query) => query.isFetching);
  const isError = queries.some((query) => query.isError);

  function handleDateChange(field: keyof ReportDateRange, value: string) {
    setDateRange((current) => ({
      ...current,
      [field]: value,
    }));
  }

  function handleRefresh() {
    void statusQuery.refetch();
    void closedQuery.refetch();
    void agentQuery.refetch();
    void slaQuery.refetch();
  }

  if (isLoading) {
    return <ReportLoadingState />;
  }

  if (
    isError ||
    !statusQuery.data ||
    !closedQuery.data ||
    !agentQuery.data ||
    !slaQuery.data
  ) {
    return <ReportErrorState onRetry={handleRefresh} />;
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
          <Typography variant="overline">Yonetici raporu</Typography>
          <Typography variant="h4">Operasyon gostergeleri</Typography>
        </Stack>
        <Stack alignItems="center" direction={{ sm: "row", xs: "column" }} spacing={1.5}>
          <TextField
            label="Baslangic"
            onChange={(event) => handleDateChange("fromDate", event.target.value)}
            size="small"
            slotProps={{ inputLabel: { shrink: true } }}
            type="date"
            value={dateRange.fromDate}
          />
          <TextField
            label="Bitis"
            onChange={(event) => handleDateChange("toDate", event.target.value)}
            size="small"
            slotProps={{ inputLabel: { shrink: true } }}
            type="date"
            value={dateRange.toDate}
          />
          <Button
            disabled={isFetching}
            onClick={handleRefresh}
            startIcon={<RefreshOutlinedIcon />}
            variant="outlined"
          >
            Yenile
          </Button>
        </Stack>
      </Stack>

      <KpiStrip
        closedReport={closedQuery.data}
        slaReport={slaQuery.data}
        statusReport={statusQuery.data}
      />

      <Box
        sx={{
          display: "grid",
          gap: 3,
          gridTemplateColumns: { lg: "1.15fr 0.85fr", xs: "1fr" },
        }}
      >
        <ClosedTicketsPanel report={closedQuery.data} />
        <StatusDistributionPanel report={statusQuery.data} />
      </Box>

      <Box
        sx={{
          display: "grid",
          gap: 3,
          gridTemplateColumns: { lg: "0.9fr 1.1fr", xs: "1fr" },
        }}
      >
        <SlaCompliancePanel report={slaQuery.data} />
        <AgentPerformancePanel rows={agentQuery.data.rows} />
      </Box>
    </Stack>
  );
}

function KpiStrip({
  closedReport,
  slaReport,
  statusReport,
}: {
  closedReport: ClosedTicketDateRangeResponse;
  slaReport: SlaComplianceReportResponse;
  statusReport: TicketStatusDistributionResponse;
}) {
  const slaPercent = toNumber(slaReport.compliancePercentage);
  const metrics = [
    {
      detail: `Uretim: ${formatDateTime(statusReport.generatedAt)}`,
      icon: <AssessmentOutlinedIcon />,
      label: "Acik ticket",
      value: statusReport.totalOpenTickets.toString(),
    },
    {
      detail: `${formatDate(closedReport.fromDate)} - ${formatDate(closedReport.toDate)}`,
      icon: <GroupsOutlinedIcon />,
      label: "Kapali ticket",
      value: closedReport.totalClosedTickets.toString(),
    },
    {
      detail: `${slaReport.metTicketCount} hedefte, ${slaReport.breachedTicketCount} ihlal`,
      icon: <ReportProblemOutlinedIcon />,
      label: "SLA uyumu",
      value: formatPercent(slaPercent),
    },
    {
      detail: "Kapali ticket ortalamasi",
      icon: <TimelapseOutlinedIcon />,
      label: "Ort. cozum",
      value: formatMinutes(closedReport.averageResolutionMinutes),
    },
  ];

  return (
    <Box
      sx={{
        display: "grid",
        gap: 2,
        gridTemplateColumns: { lg: "repeat(4, minmax(0, 1fr))", sm: "repeat(2, minmax(0, 1fr))", xs: "1fr" },
      }}
    >
      {metrics.map((metric) => (
        <Paper key={metric.label} sx={{ p: 2.5 }} variant="outlined">
          <Stack spacing={2}>
            <Stack alignItems="center" direction="row" justifyContent="space-between">
              <Typography color="text.secondary" variant="body2">
                {metric.label}
              </Typography>
              <Box sx={{ color: "primary.main", display: "flex" }}>{metric.icon}</Box>
            </Stack>
            <Stack spacing={0.5}>
              <Typography variant="h4">{metric.value}</Typography>
              <Typography color="text.secondary" variant="body2">
                {metric.detail}
              </Typography>
            </Stack>
          </Stack>
        </Paper>
      ))}
    </Box>
  );
}

function ClosedTicketsPanel({ report }: { report: ClosedTicketDateRangeResponse }) {
  const maxDailyCount = useMemo(
    () => maxValue(report.dailyCounts.map((item) => item.count)),
    [report.dailyCounts],
  );
  const maxPriorityCount = useMemo(
    () => maxValue(report.priorityCounts.map((item) => item.count)),
    [report.priorityCounts],
  );

  return (
    <Paper sx={{ p: 3 }} variant="outlined">
      <Stack spacing={3}>
        <SectionHeader
          subtitle={`${formatDate(report.fromDate)} - ${formatDate(report.toDate)}`}
          title="Kapanis hacmi"
        />
        <Box
          sx={{
            alignItems: "end",
            display: "grid",
            gap: 1,
            gridTemplateColumns: `repeat(${Math.max(report.dailyCounts.length, 1)}, minmax(12px, 1fr))`,
            minHeight: 190,
          }}
        >
          {report.dailyCounts.length === 0 ? (
            <Typography color="text.secondary">Bu aralikta kapanan ticket yok.</Typography>
          ) : (
            report.dailyCounts.map((item) => (
              <Stack alignItems="center" justifyContent="flex-end" key={item.date} spacing={1} sx={{ minWidth: 0 }}>
                <Typography color="text.secondary" variant="caption">
                  {item.count}
                </Typography>
                <Box
                  aria-label={`${formatDate(item.date)} gunluk kapanis`}
                  sx={{
                    bgcolor: "primary.main",
                    borderRadius: "4px 4px 0 0",
                    height: `${Math.max(8, (item.count / maxDailyCount) * 130)}px`,
                    width: "100%",
                  }}
                />
                <Typography color="text.secondary" noWrap variant="caption">
                  {formatShortDate(item.date)}
                </Typography>
              </Stack>
            ))
          )}
        </Box>
        <Divider />
        <Stack spacing={2}>
          <Typography variant="h6">Oncelik dagilimi</Typography>
          {report.priorityCounts.map((item) => (
            <MetricProgressRow
              key={item.priority}
              label={priorityLabel(item.priority)}
              progress={progressValue(item.count, maxPriorityCount)}
              value={`${item.count} ticket`}
            />
          ))}
          {report.priorityCounts.length === 0 && (
            <Typography color="text.secondary">Oncelik verisi yok.</Typography>
          )}
        </Stack>
      </Stack>
    </Paper>
  );
}

function StatusDistributionPanel({ report }: { report: TicketStatusDistributionResponse }) {
  const departmentCounts = report.departmentCounts ?? [];
  const teamCounts = report.teamCounts ?? [];
  const maxStatusCount = useMemo(
    () => maxValue(report.counts.map((item) => item.count)),
    [report.counts],
  );
  const maxDepartmentCount = useMemo(
    () => maxValue(departmentCounts.map((item) => item.count)),
    [departmentCounts],
  );
  const maxTeamCount = useMemo(
    () => maxValue(teamCounts.map((item) => item.count)),
    [teamCounts],
  );

  return (
    <Paper sx={{ p: 3 }} variant="outlined">
      <Stack spacing={2.5}>
        <SectionHeader
          subtitle={`Toplam acik: ${report.totalOpenTickets}`}
          title="Status dagilimi"
        />
        {report.counts.map((item) => (
          <MetricProgressRow
            key={item.status}
            label={statusLabel(item.status)}
            progress={progressValue(item.count, maxStatusCount)}
            value={`${item.count} ticket`}
          />
        ))}
        {report.counts.length === 0 && (
          <Typography color="text.secondary">Status verisi yok.</Typography>
        )}
        <Divider />
        <Typography variant="h6">Department dagilimi</Typography>
        {departmentCounts.map((item) => (
          <MetricProgressRow
            key={item.routedDepartmentId}
            label={item.routedDepartmentName ?? item.routedDepartmentCode ?? shortId(item.routedDepartmentId)}
            progress={progressValue(item.count, maxDepartmentCount)}
            value={`${item.count} ticket`}
          />
        ))}
        {departmentCounts.length === 0 && (
          <Typography color="text.secondary">Department verisi yok.</Typography>
        )}
        <Divider />
        <Typography variant="h6">Team dagilimi</Typography>
        {teamCounts.map((item) => (
          <MetricProgressRow
            key={item.assignedTeamId}
            label={shortId(item.assignedTeamId)}
            progress={progressValue(item.count, maxTeamCount)}
            value={`${item.count} ticket`}
          />
        ))}
        {teamCounts.length === 0 && (
          <Typography color="text.secondary">Team verisi yok.</Typography>
        )}
      </Stack>
    </Paper>
  );
}

function SlaCompliancePanel({ report }: { report: SlaComplianceReportResponse }) {
  const compliance = toNumber(report.compliancePercentage);

  return (
    <Paper sx={{ p: 3 }} variant="outlined">
      <Stack spacing={2.5}>
        <SectionHeader
          subtitle={`Uretim: ${formatDateTime(report.generatedAt)}`}
          title="SLA dagilimi"
        />
        <Stack spacing={1}>
          <Stack alignItems="center" direction="row" justifyContent="space-between">
            <Typography color="text.secondary">Genel uyum</Typography>
            <Typography fontWeight={600}>{formatPercent(compliance)}</Typography>
          </Stack>
          <LinearProgress
            color={slaColor(compliance)}
            sx={{ height: 8, borderRadius: 8 }}
            value={clampPercent(compliance)}
            variant="determinate"
          />
        </Stack>
        <Stack direction="row" flexWrap="wrap" gap={1}>
          <Chip label={`${report.metTicketCount} hedefte`} />
          <Chip color="warning" label={`${report.atRiskTicketCount} riskte`} variant="outlined" />
          <Chip color="error" label={`${report.breachedTicketCount} ihlal`} variant="outlined" />
          <Chip label={`${report.activeTicketCount} aktif`} variant="outlined" />
        </Stack>
        <Divider />
        <Stack spacing={2}>
          {report.priorityBreakdown.map((item) => {
            const value = toNumber(item.compliancePercentage);
            return (
              <MetricProgressRow
                color={slaColor(value)}
                key={item.priority}
                label={priorityLabel(item.priority)}
                progress={clampPercent(value)}
                value={`${formatPercent(value)} / ${item.activeTicketCount} aktif`}
              />
            );
          })}
          {report.priorityBreakdown.length === 0 && (
            <Typography color="text.secondary">SLA oncelik verisi yok.</Typography>
          )}
        </Stack>
      </Stack>
    </Paper>
  );
}

function AgentPerformancePanel({ rows }: { rows: AgentPerformanceRowResponse[] }) {
  return (
    <Paper sx={{ p: 3 }} variant="outlined">
      <Stack spacing={2}>
        <SectionHeader
          subtitle={`${rows.length} agent`}
          title="Agent performansi"
        />
        <Box sx={{ height: 420, width: "100%" }}>
          <DataGrid
            columns={agentColumns}
            disableColumnMenu
            disableRowSelectionOnClick
            getRowId={(row) => row.agentId}
            initialState={{
              pagination: {
                paginationModel: { pageSize: 5 },
              },
            }}
            pageSizeOptions={[5, 10]}
            rows={rows}
          />
        </Box>
      </Stack>
    </Paper>
  );
}

function SectionHeader({ subtitle, title }: { subtitle: string; title: string }) {
  return (
    <Stack spacing={0.5}>
      <Typography variant="h6">{title}</Typography>
      <Typography color="text.secondary" variant="body2">
        {subtitle}
      </Typography>
    </Stack>
  );
}

function MetricProgressRow({
  color = "primary",
  label,
  progress,
  value,
}: {
  color?: ProgressColor;
  label: string;
  progress: number;
  value: string;
}) {
  return (
    <Stack spacing={0.75}>
      <Stack alignItems="center" direction="row" justifyContent="space-between" spacing={2}>
        <Typography variant="body2">{label}</Typography>
        <Typography color="text.secondary" variant="body2">
          {value}
        </Typography>
      </Stack>
      <LinearProgress
        color={color}
        sx={{ height: 8, borderRadius: 8 }}
        value={clampPercent(progress)}
        variant="determinate"
      />
    </Stack>
  );
}

function createDefaultDateRange(): ReportDateRange {
  const toDate = new Date();
  const fromDate = new Date(toDate);
  fromDate.setDate(toDate.getDate() - 29);

  return {
    fromDate: toInputDate(fromDate),
    toDate: toInputDate(toDate),
  };
}

function toInputDate(value: Date) {
  return value.toISOString().slice(0, 10);
}

function toNumber(value: ReportNumericValue) {
  const parsed = typeof value === "string" ? Number(value) : value;
  return Number.isFinite(parsed) ? parsed : 0;
}

function maxValue(values: number[]) {
  return values.reduce((currentMax, value) => Math.max(currentMax, value), 0) || 1;
}

function progressValue(value: number, max: number) {
  if (max <= 0) {
    return 0;
  }

  return (value / max) * 100;
}

function clampPercent(value: number) {
  return Math.max(0, Math.min(100, value));
}

function formatPercent(value: number) {
  return `${value.toFixed(1)}%`;
}

function formatMinutes(value: ReportNumericValue) {
  const minutes = toNumber(value);
  if (minutes < 60) {
    return `${minutes.toFixed(0)} dk`;
  }

  return `${(minutes / 60).toFixed(1)} sa`;
}

function formatShortDate(value: string) {
  return new Intl.DateTimeFormat("tr-TR", {
    day: "2-digit",
    month: "2-digit",
  }).format(new Date(value));
}

function shortId(value: string) {
  if (value.length <= 12) {
    return value;
  }

  return value.slice(0, 8);
}

function statusLabel(value: string) {
  return statusLabels[value] ?? value;
}

function priorityLabel(value: string) {
  return priorityLabels[value] ?? value;
}

function slaColor(value: number): ProgressColor {
  if (value >= 90) {
    return "success";
  }

  if (value >= 75) {
    return "warning";
  }

  return "error";
}
