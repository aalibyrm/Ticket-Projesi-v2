import RefreshOutlinedIcon from "@mui/icons-material/RefreshOutlined";
import { Button, CircularProgress, Stack, Typography } from "@mui/material";

export function AgentLoadingState({ label = "Yukleniyor" }: { label?: string }) {
  return (
    <Stack alignItems="center" justifyContent="center" spacing={2} sx={{ minHeight: 280 }}>
      <CircularProgress size={28} />
      <Typography color="text.secondary">{label}</Typography>
    </Stack>
  );
}

export function AgentErrorState({
  message = "Veri alinamadi.",
  onRetry,
}: {
  message?: string;
  onRetry?: () => void;
}) {
  return (
    <Stack alignItems="center" justifyContent="center" spacing={2} sx={{ minHeight: 220 }}>
      <Typography color="text.secondary">{message}</Typography>
      {onRetry && (
        <Button onClick={onRetry} startIcon={<RefreshOutlinedIcon />} variant="outlined">
          Tekrar dene
        </Button>
      )}
    </Stack>
  );
}
