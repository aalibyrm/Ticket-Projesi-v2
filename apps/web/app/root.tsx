import "@fontsource/dm-sans/400.css";
import "@fontsource/dm-sans/500.css";
import "@fontsource/outfit/500.css";
import "@fontsource/outfit/600.css";
import "./styles/global.css";

import type { ReactNode } from "react";
import {
  Links,
  Meta,
  Outlet,
  Scripts,
  ScrollRestoration,
} from "react-router";
import { AppProviders } from "~/shared/app-providers";
import { LoadingScreen } from "~/shared/components/LoadingScreen";

export function Layout({ children }: { children: ReactNode }) {
  return (
    <html lang="tr">
      <head>
        <meta charSet="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <Meta />
        <Links />
      </head>
      <body>
        {children}
        <ScrollRestoration />
        <Scripts />
      </body>
    </html>
  );
}

export function HydrateFallback() {
  return <LoadingScreen label="Uygulama yukleniyor" />;
}

export default function App() {
  return (
    <AppProviders>
      <Outlet />
    </AppProviders>
  );
}
