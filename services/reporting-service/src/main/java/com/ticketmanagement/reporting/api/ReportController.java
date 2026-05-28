package com.ticketmanagement.reporting.api;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ticketmanagement.reporting.api.dto.AgentPerformanceReportResponse;
import com.ticketmanagement.reporting.api.dto.ClosedTicketDateRangeResponse;
import com.ticketmanagement.reporting.api.dto.SlaComplianceReportResponse;
import com.ticketmanagement.reporting.api.dto.TicketStatusDistributionResponse;
import com.ticketmanagement.reporting.application.InvalidReportRangeException;
import com.ticketmanagement.reporting.application.ReportingQueryService;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
class ReportController {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String MANAGER_ROLE = "MANAGER";
    private static final String ADMIN_ROLE = "ADMIN";

    private final ReportingQueryService reportingQueryService;

    // Manager dashboard icin acik ticket status dagilimini dondurur.
    @GetMapping("/tickets/status-distribution")
    TicketStatusDistributionResponse getOpenTicketStatusDistribution(@AuthenticationPrincipal Jwt jwt) {
        ensureReportViewerRole(jwt);
        return TicketStatusDistributionResponse.from(reportingQueryService.getOpenTicketStatusDistribution());
    }

    // Manager dashboard icin kapali ticket metriklerini tarih araligina gore dondurur.
    @GetMapping("/tickets/closed")
    ClosedTicketDateRangeResponse getClosedTicketDateRangeReport(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        ensureReportViewerRole(jwt);
        try {
            return ClosedTicketDateRangeResponse.from(reportingQueryService.getClosedTicketDateRangeReport(
                    fromDate,
                    toDate));
        } catch (InvalidReportRangeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    // Manager dashboard icin agent performans tablosunu dondurur.
    @GetMapping("/agents/performance")
    AgentPerformanceReportResponse getAgentPerformanceReport(@AuthenticationPrincipal Jwt jwt) {
        ensureReportViewerRole(jwt);
        return AgentPerformanceReportResponse.from(reportingQueryService.getAgentPerformanceReport());
    }

    // Manager dashboard icin SLA compliance yuzdesini ve priority kirilimini dondurur.
    @GetMapping("/sla/compliance")
    SlaComplianceReportResponse getSlaComplianceReport(@AuthenticationPrincipal Jwt jwt) {
        ensureReportViewerRole(jwt);
        return SlaComplianceReportResponse.from(reportingQueryService.getSlaComplianceReport());
    }

    // JWT varsa rapor endpointlerini sadece MANAGER veya ADMIN rolune acar.
    private void ensureReportViewerRole(Jwt jwt) {
        if (jwt == null) {
            return;
        }
        Object realmAccess = jwt.getClaims().get(REALM_ACCESS_CLAIM);
        if (realmAccess instanceof Map<?, ?> access
                && access.get(ROLES_CLAIM) instanceof Collection<?> roles
                && (roles.contains(MANAGER_ROLE) || roles.contains(ADMIN_ROLE))) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Report access requires MANAGER or ADMIN role");
    }
}
