import { apiClient } from "./httpClient";
import type {
  AgentPerformanceReportResponse,
  ClosedTicketDateRangeResponse,
  SlaComplianceReportResponse,
  TicketStatusDistributionResponse
} from "./mobileApiTypes";

export async function getTicketStatusDistribution() {
  const response = await apiClient.get<TicketStatusDistributionResponse>("/api/reports/tickets/status-distribution");
  return response.data;
}

export async function getClosedTicketReport(fromDate: string, toDate: string) {
  const response = await apiClient.get<ClosedTicketDateRangeResponse>("/api/reports/tickets/closed", {
    params: { fromDate, toDate }
  });
  return response.data;
}

export async function getAgentPerformanceReport() {
  const response = await apiClient.get<AgentPerformanceReportResponse>("/api/reports/agents/performance");
  return response.data;
}

export async function getSlaComplianceReport() {
  const response = await apiClient.get<SlaComplianceReportResponse>("/api/reports/sla/compliance");
  return response.data;
}
