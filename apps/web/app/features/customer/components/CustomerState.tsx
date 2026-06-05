import RefreshOutlinedIcon from "@mui/icons-material/RefreshOutlined";
import {
  Alert,
  Skeleton,
  Stack,
  Typography,
} from "@mui/material";
import { TmButton, TmSurface } from "~/shared/design-system";

export function CustomerLoadingState({ label = "Veriler yukleniyor" }: { label?: string }) {
  return (
    <TmSurface sx={{ p: 3 }}>
      <Stack spacing={1.5}>
        <Typography color="text.secondary" variant="body2">
          {label}
        </Typography>
        <Skeleton height={36} variant="rounded" />
        <Skeleton height={36} variant="rounded" />
        <Skeleton height={36} variant="rounded" />
      </Stack>
    </TmSurface>
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
          <TmButton color="inherit" onClick={onRetry} size="small" startIcon={<RefreshOutlinedIcon />}>
            Tekrar dene
          </TmButton>
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
    <TmSurface sx={{ p: 4, textAlign: "center" }}>
      <Typography color="text.secondary">{message}</Typography>
    </TmSurface>
  );
}
