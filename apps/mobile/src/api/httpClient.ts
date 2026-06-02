import axios, { AxiosHeaders, type InternalAxiosRequestConfig } from "axios";
import { mobileEnv } from "../config/env";

export type AccessTokenProvider = () => Promise<string | undefined> | string | undefined;

let accessTokenProvider: AccessTokenProvider | undefined;

export function setAccessTokenProvider(provider: AccessTokenProvider | undefined) {
  accessTokenProvider = provider;
}

export const apiClient = axios.create({
  baseURL: mobileEnv.apiBaseUrl,
  timeout: 15000
});

apiClient.interceptors.request.use(async (config) => {
  const token = await accessTokenProvider?.();
  const headers = AxiosHeaders.from(config.headers);

  headers.set("X-Client-Platform", "mobile");

  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  return {
    ...config,
    headers
  } satisfies InternalAxiosRequestConfig;
});
