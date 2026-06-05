import { Paper, type PaperProps } from "@mui/material";

export function TmSurface({ children, ...props }: PaperProps) {
  return (
    <Paper {...props} elevation={0}>
      {children}
    </Paper>
  );
}
