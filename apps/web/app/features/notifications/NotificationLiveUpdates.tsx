import { useQueryClient } from "@tanstack/react-query";
import { useEffect } from "react";
import { getAccessToken } from "~/features/auth/authService";
import { selectAuthStatus } from "~/features/auth/authSlice";
import { agentQueryKeys } from "~/features/agent/agentQueries";
import { customerQueryKeys } from "~/features/customer/customerQueries";
import { versionedApiPath } from "~/shared/api/httpClient";
import { useAppSelector } from "~/shared/store/hooks";
import { appConfig } from "~/shared/config/appConfig";

interface NotificationLiveEvent {
  eventType: string;
  notification?: {
    id: string;
    read: boolean;
    type: string;
  };
}

const reconnectDelayMs = 5_000;

export function NotificationLiveUpdates() {
  const authStatus = useAppSelector(selectAuthStatus);
  const queryClient = useQueryClient();

  useEffect(() => {
    if (authStatus !== "authenticated") {
      return undefined;
    }

    const abortController = new AbortController();
    let reconnectTimer: number | undefined;
    let stopped = false;

    async function connect() {
      try {
        const token = await getAccessToken();
        if (!token || stopped) {
          return;
        }

        const response = await fetch(`${appConfig.apiBaseUrl}${versionedApiPath("/api/notifications/stream")}`, {
          headers: {
            Accept: "text/event-stream",
            Authorization: `Bearer ${token}`,
            "X-Correlation-Id": createCorrelationId(),
          },
          signal: abortController.signal,
        });

        if (!response.ok || !response.body) {
          throw new Error(`Notification live stream failed with HTTP ${response.status}`);
        }

        await consumeSseStream(response.body, abortController.signal, (event) => {
          handleLiveEvent(event, queryClient);
        });

        if (!stopped) {
          scheduleReconnect();
        }
      } catch (error) {
        if (!stopped && !abortController.signal.aborted) {
          scheduleReconnect();
        }
      }
    }

    function scheduleReconnect() {
      window.clearTimeout(reconnectTimer);
      reconnectTimer = window.setTimeout(() => void connect(), reconnectDelayMs);
    }

    void connect();

    return () => {
      stopped = true;
      abortController.abort();
      window.clearTimeout(reconnectTimer);
    };
  }, [authStatus, queryClient]);

  return null;
}

async function consumeSseStream(
  body: ReadableStream<Uint8Array>,
  signal: AbortSignal,
  onEvent: (event: NotificationLiveEvent) => void,
) {
  const reader = body.getReader();
  const decoder = new TextDecoder();
  let buffer = "";

  while (!signal.aborted) {
    const { done, value } = await reader.read();
    if (done) {
      break;
    }

    buffer += decoder.decode(value, { stream: true });
    buffer = buffer.replace(/\r\n/g, "\n");

    let separatorIndex = buffer.indexOf("\n\n");
    while (separatorIndex >= 0) {
      const rawEvent = buffer.slice(0, separatorIndex);
      buffer = buffer.slice(separatorIndex + 2);
      const event = parseSseEvent(rawEvent);
      if (event) {
        onEvent(event);
      }
      separatorIndex = buffer.indexOf("\n\n");
    }
  }
}

function parseSseEvent(rawEvent: string) {
  const dataLines = rawEvent
    .split("\n")
    .filter((line) => line.startsWith("data:"))
    .map((line) => line.slice(5).trimStart());

  if (dataLines.length === 0) {
    return undefined;
  }

  try {
    return JSON.parse(dataLines.join("\n")) as NotificationLiveEvent;
  } catch {
    return undefined;
  }
}

function handleLiveEvent(event: NotificationLiveEvent, queryClient: ReturnType<typeof useQueryClient>) {
  if (event.eventType === "stream.connected" || event.eventType === "stream.heartbeat") {
    return;
  }

  invalidateNotificationQueries(queryClient);

  if (event.notification?.type === "TICKET_EXTERNAL_COMMENT_ADDED") {
    invalidateConversationQueries(queryClient);
  }
}

function invalidateNotificationQueries(queryClient: ReturnType<typeof useQueryClient>) {
  void queryClient.invalidateQueries({
    predicate: (query) => isQueryKeyPrefix(query.queryKey, "customer", "notifications"),
  });
}

function invalidateConversationQueries(queryClient: ReturnType<typeof useQueryClient>) {
  void queryClient.invalidateQueries({
    predicate: (query) =>
      (isQueryKeyPrefix(query.queryKey, "customer", "ticket")
        || isQueryKeyPrefix(query.queryKey, "agent", "ticket"))
      && query.queryKey.includes("comments"),
  });
  void queryClient.invalidateQueries({ queryKey: customerQueryKeys.tickets });
  void queryClient.invalidateQueries({ queryKey: agentQueryKeys.tickets });
}

function isQueryKeyPrefix(queryKey: readonly unknown[], first: string, second: string) {
  return queryKey[0] === first && queryKey[1] === second;
}

function createCorrelationId() {
  if (globalThis.crypto?.randomUUID) {
    return globalThis.crypto.randomUUID();
  }

  return `web-sse-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}
