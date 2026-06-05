import { Chip, type ChipProps } from "@mui/material";
import { tmTokens } from "~/shared/theme/tmTokens";

type TmChipTone = "active" | "danger" | "neutral" | "resolved" | "waiting";

const toneStyles: Record<TmChipTone, { backgroundColor: string; color: string }> = {
  active: {
    backgroundColor: tmTokens.colors.surfaceContainer,
    color: tmTokens.colors.primaryContainer,
  },
  danger: {
    backgroundColor: "#ffdad6",
    color: tmTokens.colors.error,
  },
  neutral: {
    backgroundColor: tmTokens.colors.surfaceContainer,
    color: tmTokens.colors.secondary,
  },
  resolved: {
    backgroundColor: tmTokens.colors.surfaceContainer,
    color: tmTokens.colors.onSurface,
  },
  waiting: {
    backgroundColor: tmTokens.colors.surfaceLow,
    color: tmTokens.colors.secondary,
  },
};

interface TmStatusChipProps extends ChipProps {
  tone?: TmChipTone;
}

export function TmStatusChip({ tone = "neutral", sx, ...props }: TmStatusChipProps) {
  const styles = toneStyles[tone];

  return (
    <Chip
      {...props}
      size="small"
      sx={[
        {
          ...styles,
          border: `1px solid ${tmTokens.colors.border}`,
          borderRadius: `${tmTokens.radius.md}px`,
          height: 28,
          px: 0.5,
          "& .MuiChip-label": {
            px: 1,
          },
        },
        ...(Array.isArray(sx) ? sx : sx ? [sx] : []),
      ]}
    />
  );
}
