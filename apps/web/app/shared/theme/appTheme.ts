import { createTheme } from "@mui/material/styles";
import type {} from "@mui/x-data-grid/themeAugmentation";

export const appTheme = createTheme({
  palette: {
    mode: "light",
    background: {
      default: "#f9f9fe",
      paper: "#ffffff",
    },
    divider: "#e8e8ed",
    primary: {
      main: "#7f0900",
      dark: "#5f0700",
      light: "#aa1101",
      contrastText: "#ffffff",
    },
    error: {
      main: "#ba1a1a",
    },
    text: {
      primary: "#1a1c1f",
      secondary: "#5f5e5e",
    },
  },
  shape: {
    borderRadius: 8,
  },
  typography: {
    fontFamily: "\"DM Sans\", Arial, sans-serif",
    h1: {
      fontFamily: "\"Outfit\", Arial, sans-serif",
      fontWeight: 600,
      letterSpacing: 0,
    },
    h2: {
      fontFamily: "\"Outfit\", Arial, sans-serif",
      fontWeight: 600,
      letterSpacing: 0,
    },
    h3: {
      fontFamily: "\"Outfit\", Arial, sans-serif",
      fontWeight: 600,
      letterSpacing: 0,
    },
    h4: {
      fontFamily: "\"Outfit\", Arial, sans-serif",
      fontSize: "2rem",
      fontWeight: 600,
      letterSpacing: 0,
    },
    h5: {
      fontFamily: "\"Outfit\", Arial, sans-serif",
      fontWeight: 600,
      letterSpacing: 0,
    },
    h6: {
      fontFamily: "\"Outfit\", Arial, sans-serif",
      fontWeight: 600,
      letterSpacing: 0,
    },
    button: {
      fontWeight: 600,
      letterSpacing: 0,
      textTransform: "none",
    },
    overline: {
      color: "#5f5e5e",
      fontWeight: 600,
      letterSpacing: 0,
      textTransform: "uppercase",
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
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
          borderColor: "#e8e8ed",
        },
      },
    },
  },
});
