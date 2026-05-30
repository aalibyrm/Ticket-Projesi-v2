import { Box, Chip, Paper } from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";

interface TicketRow {
  id: number;
  number: string;
  priority: "LOW" | "MEDIUM" | "HIGH";
  status: "OPEN" | "IN_PROGRESS" | "RESOLVED";
  subject: string;
  updatedAt: string;
}

const rows: TicketRow[] = [
  {
    id: 1,
    number: "TCK-2026-0001",
    priority: "HIGH",
    status: "IN_PROGRESS",
    subject: "VPN baglantisi kesiliyor",
    updatedAt: "2026-05-30 10:12",
  },
  {
    id: 2,
    number: "TCK-2026-0002",
    priority: "MEDIUM",
    status: "OPEN",
    subject: "E-posta teslim problemi",
    updatedAt: "2026-05-30 09:44",
  },
  {
    id: 3,
    number: "TCK-2026-0003",
    priority: "LOW",
    status: "RESOLVED",
    subject: "Yetki talebi",
    updatedAt: "2026-05-29 17:08",
  },
];

const columns: GridColDef<TicketRow>[] = [
  { field: "number", flex: 1, headerName: "Talep No", minWidth: 150 },
  { field: "subject", flex: 2, headerName: "Konu", minWidth: 220 },
  {
    field: "priority",
    headerName: "Oncelik",
    minWidth: 130,
    renderCell: ({ value }) => <Chip label={value} size="small" />,
  },
  {
    field: "status",
    headerName: "Durum",
    minWidth: 160,
    renderCell: ({ value }) => <Chip color="primary" label={value} size="small" variant="outlined" />,
  },
  { field: "updatedAt", flex: 1, headerName: "Guncelleme", minWidth: 170 },
];

export function TicketPreviewGrid() {
  return (
    <Paper sx={{ p: 2 }}>
      <Box sx={{ height: 440, width: "100%" }}>
        <DataGrid
          columns={columns}
          disableRowSelectionOnClick
          initialState={{
            pagination: {
              paginationModel: { pageSize: 10 },
            },
          }}
          pageSizeOptions={[10, 25]}
          rows={rows}
        />
      </Box>
    </Paper>
  );
}
