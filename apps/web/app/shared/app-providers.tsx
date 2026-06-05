import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";
import { Provider as ReduxProvider } from "react-redux";
import { AuthBootstrap } from "~/features/auth/AuthBootstrap";
import { NotificationLiveUpdates } from "~/features/notifications/NotificationLiveUpdates";
import { store } from "~/shared/store/store";
import { UiProviders } from "~/shared/theme/UiProviders";

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
  return (
    <ReduxProvider store={store}>
      <QueryClientProvider client={queryClient}>
        <UiProviders>
          <AuthBootstrap />
          <NotificationLiveUpdates />
          {children}
        </UiProviders>
      </QueryClientProvider>
    </ReduxProvider>
  );
}
