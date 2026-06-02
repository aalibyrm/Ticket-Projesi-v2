import { apiClient } from "./httpClient";

export interface GatewayHealthResponse {
  status: string;
}

export async function fetchGatewayHealth() {
  const response = await apiClient.get<GatewayHealthResponse>("/actuator/health");
  return response.data;
}
