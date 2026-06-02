import type {
  AgentPerformanceReportResponse,
  ClosedTicketDateRangeResponse,
  ReportDateRange,
  SlaComplianceReportResponse,
  TicketStatusDistributionResponse,
} from "~/features/reports/reportTypes";
import { apiClient } from "~/shared/api/httpClient";

export async function getTicketStatusDistribution() {
  const response = await apiClient.get<TicketStatusDistributionResponse>("/api/reports/tickets/status-distribution");
  return response.data;
}

export async function getClosedTicketReport(range: ReportDateRange) {
  const response = await apiClient.get<ClosedTicketDateRangeResponse>("/api/reports/tickets/closed", {
    params: range,
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
