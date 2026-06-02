package com.ticketmanagement.ticket.api;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketmanagement.ticket.api.dto.DepartmentResponse;
import com.ticketmanagement.ticket.api.dto.SupportTeamResponse;
import com.ticketmanagement.ticket.api.dto.TeamMemberResponse;
import com.ticketmanagement.ticket.application.OrganizationQueryService;

@RestController
@RequestMapping("/api/organization")
@RequiredArgsConstructor
class OrganizationController {

    private final OrganizationQueryService organizationQueryService;

    // Aktif departmanlari ve ekiplerini organizasyon katalogu olarak dondurur.
    @GetMapping("/departments")
    List<DepartmentResponse> listDepartments() {
        return organizationQueryService.listActiveDepartments();
    }

    // Aktif destek ekiplerini departman bilgisiyle birlikte dondurur.
    @GetMapping("/teams")
    List<SupportTeamResponse> listTeams() {
        return organizationQueryService.listActiveTeams();
    }

    // Secilen ekibin aktif uyelerini dondurur.
    @GetMapping("/teams/{teamId}/members")
    List<TeamMemberResponse> listTeamMembers(@PathVariable UUID teamId) {
        return organizationQueryService.listActiveTeamMembers(teamId);
    }
}
