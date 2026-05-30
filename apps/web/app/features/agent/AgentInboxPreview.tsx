import {
  Chip,
  Paper,
  Stack,
  Typography,
} from "@mui/material";

const items = [
  { count: 12, label: "Atanan talepler", tone: "primary" as const },
  { count: 4, label: "SLA riski", tone: "error" as const },
  { count: 7, label: "Yanitsiz musteri mesaji", tone: "default" as const },
];

export function AgentInboxPreview() {
  return (
    <Stack spacing={3}>
      <Stack spacing={0.75}>
        <Typography variant="overline">Temsilci paneli</Typography>
        <Typography variant="h4">Is kuyrugu</Typography>
      </Stack>
      <Stack direction={{ md: "row", xs: "column" }} spacing={2}>
        {items.map((item) => (
          <Paper key={item.label} sx={{ flex: 1, p: 3 }}>
            <Stack spacing={2}>
              <Chip color={item.tone} label={item.label} sx={{ alignSelf: "flex-start" }} />
              <Typography variant="h3">{item.count}</Typography>
            </Stack>
          </Paper>
        ))}
      </Stack>
    </Stack>
  );
}
