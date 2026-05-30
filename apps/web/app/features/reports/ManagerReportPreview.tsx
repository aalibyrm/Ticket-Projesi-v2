import {
  LinearProgress,
  Paper,
  Stack,
  Typography,
} from "@mui/material";

const metrics = [
  { label: "SLA uyumu", value: 84 },
  { label: "Ilk yanit hedefi", value: 76 },
  { label: "Cozum hedefi", value: 69 },
];

export function ManagerReportPreview() {
  return (
    <Stack spacing={3}>
      <Stack spacing={0.75}>
        <Typography variant="overline">Yonetici raporu</Typography>
        <Typography variant="h4">Operasyon ozeti</Typography>
      </Stack>
      <Stack spacing={2}>
        {metrics.map((metric) => (
          <Paper key={metric.label} sx={{ p: 3 }}>
            <Stack spacing={1}>
              <Stack direction="row" justifyContent="space-between">
                <Typography fontWeight={600}>{metric.label}</Typography>
                <Typography color="text.secondary">{metric.value}%</Typography>
              </Stack>
              <LinearProgress
                color={metric.value > 80 ? "primary" : "error"}
                value={metric.value}
                variant="determinate"
              />
            </Stack>
          </Paper>
        ))}
      </Stack>
    </Stack>
  );
}
