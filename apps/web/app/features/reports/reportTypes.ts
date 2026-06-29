export type ReportNumericValue = number | string;

export interface TicketStatusCountResponse {
  count: number;
  status: string;
}

export interface DepartmentTicketCountResponse {
  count: number;
  routedDepartmentCode?: string | null;
  routedDepartmentId: string;
  routedDepartmentName?: string | null;
}

export interface TeamTicketCountResponse {
  assignedTeamId: string;
  assignedTeamCode?: string | null;
  assignedTeamName?: string | null;
  count: number;
}

export interface TicketStatusDistributionResponse {
  counts: TicketStatusCountResponse[];
  departmentCounts: DepartmentTicketCountResponse[];
  generatedAt: string;
  teamCounts: TeamTicketCountResponse[];
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
  agentDisplayName?: string | null;
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
