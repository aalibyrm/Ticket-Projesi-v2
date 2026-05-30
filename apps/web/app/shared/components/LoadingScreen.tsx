import { Box, CircularProgress, Stack, Typography } from "@mui/material";

export function LoadingScreen({ label }: { label: string }) {
  return (
    <Box className="app-hydrate">
      <Stack alignItems="center" spacing={2}>
        <CircularProgress color="primary" size={28} />
        <Typography color="text.secondary" variant="body2">
          {label}
        </Typography>
      </Stack>
    </Box>
  );
}
