package com.ticketmanagement.ticket.application;

import java.util.UUID;

public interface AgentSummaryLookupPort {

    AgentSummaryMetrics getAgentSummary(UUID agentId);
}
