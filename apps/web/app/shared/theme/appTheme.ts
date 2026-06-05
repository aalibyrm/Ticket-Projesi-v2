import { createTheme } from "@mui/material/styles";
import type {} from "@mui/x-data-grid/themeAugmentation";
import { tmTokens } from "~/shared/theme/tmTokens";

export const appTheme = createTheme({
  palette: {
    mode: "light",
    background: {
      default: tmTokens.colors.background,
      paper: tmTokens.colors.surfaceLowest,
    },
    divider: tmTokens.colors.border,
    primary: {
      main: tmTokens.colors.primaryContainer,
      dark: "#5f0700",
      light: tmTokens.colors.primaryContainer,
      contrastText: "#ffffff",
    },
    error: {
      main: tmTokens.colors.error,
    },
    text: {
      primary: tmTokens.colors.onSurface,
      secondary: tmTokens.colors.secondary,
    },
  },
  shape: {
    borderRadius: tmTokens.radius.md,
  },
  typography: {
    fontFamily: "\"DM Sans\", Arial, sans-serif",
    h1: {
      ...tmTokens.typography.headlineXl,
    },
    h2: {
      ...tmTokens.typography.headlineLg,
    },
    h3: {
      ...tmTokens.typography.headlineMd,
    },
    h4: {
      ...tmTokens.typography.headlineXl,
    },
    h5: {
      ...tmTokens.typography.headlineLg,
    },
    h6: {
      ...tmTokens.typography.headlineSm,
    },
    button: {
      ...tmTokens.typography.bodyMdBold,
      letterSpacing: 0,
      textTransform: "none",
    },
    overline: {
      ...tmTokens.typography.labelSm,
      color: tmTokens.colors.secondary,
      textTransform: "uppercase",
    },
    body1: tmTokens.typography.bodyMd,
    body2: tmTokens.typography.bodyMd,
  },
  components: {
    MuiButton: {
      defaultProps: {
        disableElevation: true,
      },
      styleOverrides: {
        root: {
          borderRadius: tmTokens.radius.md,
          boxShadow: "none",
          minHeight: 40,
          paddingLeft: 16,
          paddingRight: 16,
          textTransform: "none",
          "&:hover": {
            boxShadow: "none",
          },
        },
        containedPrimary: {
          backgroundColor: tmTokens.colors.primaryContainer,
          "&:hover": {
            backgroundColor: tmTokens.colors.primary,
          },
        },
        outlined: {
          backgroundColor: tmTokens.colors.surfaceLowest,
          borderColor: tmTokens.colors.border,
          color: tmTokens.colors.onSurface,
          "&:hover": {
            backgroundColor: tmTokens.colors.surfaceLow,
            borderColor: tmTokens.colors.border,
          },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          border: "1px solid #e8e8ed",
          boxShadow: "none",
        },
      },
    },
    MuiDataGrid: {
      styleOverrides: {
        root: {
          borderColor: tmTokens.colors.border,
          borderRadius: tmTokens.radius.md,
          boxShadow: "none",
        },
        columnHeaders: {
          backgroundColor: tmTokens.colors.surfaceLow,
        },
        row: {
          minHeight: "64px !important",
        },
      },
    },
    MuiIconButton: {
      styleOverrides: {
        root: {
          borderRadius: tmTokens.radius.md,
          color: tmTokens.colors.secondary,
          "&:hover": {
            backgroundColor: tmTokens.colors.surfaceLow,
            color: tmTokens.colors.primaryContainer,
          },
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: "none",
          border: `1px solid ${tmTokens.colors.border}`,
          boxShadow: "none",
        },
      },
    },
    MuiTab: {
      styleOverrides: {
        root: {
          ...tmTokens.typography.bodyMdBold,
          color: tmTokens.colors.secondary,
          minHeight: 36,
          paddingLeft: 4,
          paddingRight: 4,
          textTransform: "none",
          "&.Mui-selected": {
            color: tmTokens.colors.onSurface,
          },
        },
      },
    },
    MuiTabs: {
      styleOverrides: {
        indicator: {
          backgroundColor: tmTokens.colors.primaryContainer,
          height: 2,
        },
      },
    },
  },
});
