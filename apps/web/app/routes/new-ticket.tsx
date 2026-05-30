import { Stack, Typography } from "@mui/material";
import { NewTicketForm } from "~/features/tickets/NewTicketForm";

export default function NewTicketRoute() {
  return (
    <Stack spacing={3}>
      <Stack spacing={0.75}>
        <Typography variant="overline">Yeni talep</Typography>
        <Typography variant="h4">Destek talebi olustur</Typography>
      </Stack>
      <NewTicketForm />
    </Stack>
  );
}
