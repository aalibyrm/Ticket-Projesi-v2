import { Stack, Typography } from "@mui/material";
import { TicketPreviewGrid } from "~/features/tickets/TicketPreviewGrid";

export default function TicketsRoute() {
  return (
    <Stack spacing={3}>
      <Stack spacing={0.75}>
        <Typography variant="overline">Musteri portali</Typography>
        <Typography variant="h4">Taleplerim</Typography>
      </Stack>
      <TicketPreviewGrid />
    </Stack>
  );
}
