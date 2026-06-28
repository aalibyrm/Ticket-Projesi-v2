package com.ticketmanagement.reporting.api;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketmanagement.reporting.api.dto.AgentSummaryResponse;
import com.ticketmanagement.reporting.application.ReportingQueryService;

@RestController
@RequestMapping("/internal/reports")
@RequiredArgsConstructor
class InternalReportController {

    private final ReportingQueryService reportingQueryService;

    // Servis ici kullanim icin tek agent'in cozum ve SLA ozetini dondurur.
    @GetMapping("/agents/{agentId}/summary")
    AgentSummaryResponse getAgentSummary(@PathVariable UUID agentId) {
        return AgentSummaryResponse.from(reportingQueryService.getAgentSummary(agentId));
    }
}
