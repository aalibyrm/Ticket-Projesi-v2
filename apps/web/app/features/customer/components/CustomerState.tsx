import RefreshOutlinedIcon from "@mui/icons-material/RefreshOutlined";
import {
  Alert,
  Button,
  Paper,
  Skeleton,
  Stack,
  Typography,
} from "@mui/material";

export function CustomerLoadingState({ label = "Veriler yukleniyor" }: { label?: string }) {
  return (
    <Paper sx={{ p: 3 }}>
      <Stack spacing={1.5}>
        <Typography color="text.secondary" variant="body2">
          {label}
        </Typography>
        <Skeleton height={36} variant="rounded" />
        <Skeleton height={36} variant="rounded" />
        <Skeleton height={36} variant="rounded" />
      </Stack>
    </Paper>
  );
}

export function CustomerErrorState({
  message = "Veri alinirken hata olustu.",
  onRetry,
}: {
  message?: string;
  onRetry?: () => void;
}) {
  return (
    <Alert
      action={
        onRetry ? (
          <Button color="inherit" onClick={onRetry} size="small" startIcon={<RefreshOutlinedIcon />}>
            Tekrar dene
          </Button>
        ) : undefined
      }
      severity="error"
      variant="outlined"
    >
      {message}
    </Alert>
  );
}

export function CustomerEmptyState({ message }: { message: string }) {
  return (
    <Paper sx={{ p: 4, textAlign: "center" }}>
      <Typography color="text.secondary">{message}</Typography>
    </Paper>
  );
}
