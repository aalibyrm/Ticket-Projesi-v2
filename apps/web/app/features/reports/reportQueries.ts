import { useQuery } from "@tanstack/react-query";
import {
  getAgentPerformanceReport,
  getClosedTicketReport,
  getSlaComplianceReport,
  getTicketStatusDistribution,
} from "~/features/reports/reportApi";
import type { ReportDateRange } from "~/features/reports/reportTypes";

export const reportQueryKeys = {
  agentPerformance: ["reports", "agents", "performance"] as const,
  closedTickets: (range: ReportDateRange) => ["reports", "tickets", "closed", range.fromDate, range.toDate] as const,
  slaCompliance: ["reports", "sla", "compliance"] as const,
  statusDistribution: ["reports", "tickets", "status-distribution"] as const,
};

export function useTicketStatusDistribution() {
  return useQuery({
    queryFn: getTicketStatusDistribution,
    queryKey: reportQueryKeys.statusDistribution,
  });
}

export function useClosedTicketReport(range: ReportDateRange) {
  return useQuery({
    queryFn: () => getClosedTicketReport(range),
    queryKey: reportQueryKeys.closedTickets(range),
  });
}

export function useAgentPerformanceReport() {
  return useQuery({
    queryFn: getAgentPerformanceReport,
    queryKey: reportQueryKeys.agentPerformance,
  });
}

export function useSlaComplianceReport() {
  return useQuery({
    queryFn: getSlaComplianceReport,
    queryKey: reportQueryKeys.slaCompliance,
  });
}
