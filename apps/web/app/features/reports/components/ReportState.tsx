import RefreshOutlinedIcon from "@mui/icons-material/RefreshOutlined";
import { Button, CircularProgress, Stack, Typography } from "@mui/material";

export function ReportLoadingState() {
  return (
    <Stack alignItems="center" justifyContent="center" spacing={2} sx={{ minHeight: 360 }}>
      <CircularProgress size={28} />
      <Typography color="text.secondary">Raporlar yukleniyor</Typography>
    </Stack>
  );
}

export function ReportErrorState({ onRetry }: { onRetry: () => void }) {
  return (
    <Stack alignItems="center" justifyContent="center" spacing={2} sx={{ minHeight: 360 }}>
      <Typography color="text.secondary">Rapor verisi alinamadi.</Typography>
      <Button onClick={onRetry} startIcon={<RefreshOutlinedIcon />} variant="outlined">
        Tekrar dene
      </Button>
    </Stack>
  );
}
