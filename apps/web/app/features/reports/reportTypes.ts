export type ReportNumericValue = number | string;

export interface TicketStatusCountResponse {
  count: number;
  status: string;
}

export interface TicketStatusDistributionResponse {
  counts: TicketStatusCountResponse[];
  generatedAt: string;
  totalOpenTickets: number;
}

export interface ClosedTicketDailyCountResponse {
  count: number;
  date: string;
}

export interface ClosedTicketPriorityCountResponse {
  count: number;
  priority: string;
}

export interface ClosedTicketDateRangeResponse {
  averageResolutionMinutes: ReportNumericValue;
  dailyCounts: ClosedTicketDailyCountResponse[];
  fromDate: string;
  generatedAt: string;
  priorityCounts: ClosedTicketPriorityCountResponse[];
  toDate: string;
  totalClosedTickets: number;
}

export interface AgentPerformanceRowResponse {
  agentId: string;
  assignedTicketCount: number;
  averageResolutionMinutes: ReportNumericValue;
  resolvedTicketCount: number;
  totalWorklogMinutes: number;
}

export interface AgentPerformanceReportResponse {
  generatedAt: string;
  rows: AgentPerformanceRowResponse[];
}

export interface SlaCompliancePriorityResponse {
  activeTicketCount: number;
  atRiskTicketCount: number;
  breachedTicketCount: number;
  compliancePercentage: ReportNumericValue;
  metTicketCount: number;
  priority: string;
}

export interface SlaComplianceReportResponse {
  activeTicketCount: number;
  atRiskTicketCount: number;
  breachedTicketCount: number;
  compliancePercentage: ReportNumericValue;
  generatedAt: string;
  metTicketCount: number;
  priorityBreakdown: SlaCompliancePriorityResponse[];
}

export interface ReportDateRange {
  fromDate: string;
  toDate: string;
}
