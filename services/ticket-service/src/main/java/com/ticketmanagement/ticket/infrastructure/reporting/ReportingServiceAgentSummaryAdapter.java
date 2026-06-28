package com.ticketmanagement.ticket.infrastructure.reporting;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.ticketmanagement.ticket.application.AgentSummaryLookupPort;
import com.ticketmanagement.ticket.application.AgentSummaryMetrics;

@Component
@RequiredArgsConstructor
class ReportingServiceAgentSummaryAdapter implements AgentSummaryLookupPort {

    @Qualifier("reportingServiceRestClient")
    private final RestClient reportingServiceRestClient;

    @Override
    public AgentSummaryMetrics getAgentSummary(UUID agentId) {
        try {
            ReportingAgentSummaryResponse response = reportingServiceRestClient.get()
                    .uri("/internal/reports/agents/{agentId}/summary", agentId)
                    .retrieve()
                    .body(ReportingAgentSummaryResponse.class);
            if (response == null) {
                return AgentSummaryMetrics.empty(agentId);
            }
            return new AgentSummaryMetrics(
                    response.agentId(),
                    response.resolvedTicketCount(),
                    response.slaMetTicketCount(),
                    response.slaBreachedTicketCount(),
                    response.slaCompliancePercentage());
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Agent reporting summary service unavailable",
                    exception);
        }
    }

    private record ReportingAgentSummaryResponse(
            UUID agentId,
            long resolvedTicketCount,
            long slaMetTicketCount,
            long slaBreachedTicketCount,
            BigDecimal slaCompliancePercentage) {
    }
}
