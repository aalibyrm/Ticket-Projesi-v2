import { CssBaseline } from "@mui/material";
import { ThemeProvider } from "@mui/material/styles";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useEffect, type ReactNode } from "react";
import { Provider as ReduxProvider } from "react-redux";
import { AuthBootstrap } from "~/features/auth/AuthBootstrap";
import { store } from "~/shared/store/store";
import { appTheme } from "~/shared/theme/appTheme";

declare global {
  interface Window {
    __TICKET_WEB_HYDRATED__?: boolean;
  }
}

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 30_000,
    },
  },
});

export function AppProviders({ children }: { children: ReactNode }) {
  useEffect(() => {
    window.__TICKET_WEB_HYDRATED__ = true;

    return () => {
      window.__TICKET_WEB_HYDRATED__ = false;
    };
  }, []);

  return (
    <ReduxProvider store={store}>
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={appTheme}>
          <CssBaseline />
          <AuthBootstrap />
          {children}
        </ThemeProvider>
      </QueryClientProvider>
    </ReduxProvider>
  );
}
