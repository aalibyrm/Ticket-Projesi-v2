import createCache from "@emotion/cache";
import { CacheProvider } from "@emotion/react";
import { CssBaseline } from "@mui/material";
import { ThemeProvider } from "@mui/material/styles";
import type { ReactNode } from "react";
import { useMemo } from "react";
import { appTheme } from "~/shared/theme/appTheme";

export function UiProviders({ children }: { children: ReactNode }) {
  const emotionCache = useMemo(() => createCache({ key: "mui" }), []);

  return (
    <CacheProvider value={emotionCache}>
      <ThemeProvider theme={appTheme}>
        <CssBaseline />
        {children}
      </ThemeProvider>
    </CacheProvider>
  );
}
