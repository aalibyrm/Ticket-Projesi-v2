import axios, { AxiosHeaders } from "axios";
import { appConfig } from "~/shared/config/appConfig";

type AccessTokenProvider = () => Promise<string | undefined>;

let accessTokenProvider: AccessTokenProvider | undefined;

export function setAccessTokenProvider(provider: AccessTokenProvider | undefined) {
  accessTokenProvider = provider;
}

export const apiClient = axios.create({
  baseURL: appConfig.apiBaseUrl,
  timeout: 10_000,
});

apiClient.interceptors.request.use(async (config) => {
  const headers = AxiosHeaders.from(config.headers);
  headers.set("X-Correlation-Id", createCorrelationId());

  const token = await accessTokenProvider?.();
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  config.headers = headers;
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (import.meta.env.DEV && axios.isAxiosError(error)) {
      console.warn("API request failed", {
        code: error.code,
        method: error.config?.method,
        path: error.config?.url,
        status: error.response?.status,
        correlationId: error.response?.headers?.["x-correlation-id"],
      });
    }

    return Promise.reject(error);
  },
);

function createCorrelationId() {
  if (globalThis.crypto?.randomUUID) {
    return globalThis.crypto.randomUUID();
  }

  return `web-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}
