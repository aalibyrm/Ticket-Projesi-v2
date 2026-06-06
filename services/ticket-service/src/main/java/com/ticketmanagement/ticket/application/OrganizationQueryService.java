package com.ticketmanagement.ticket.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.ticket.api.dto.DepartmentResponse;
import com.ticketmanagement.ticket.api.dto.SupportTeamResponse;
import com.ticketmanagement.ticket.api.dto.TeamMemberResponse;
import com.ticketmanagement.ticket.infrastructure.persistence.DepartmentEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.DepartmentJpaRepository;
import com.ticketmanagement.ticket.infrastructure.persistence.SupportTeamEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.SupportTeamJpaRepository;
import com.ticketmanagement.ticket.infrastructure.persistence.TeamMemberEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TeamMemberJpaRepository;

@Service
@RequiredArgsConstructor
public class OrganizationQueryService {

    private final DepartmentJpaRepository departmentRepository;
    private final SupportTeamJpaRepository teamRepository;
    private final TeamMemberJpaRepository teamMemberRepository;
    private final ActorProfileDirectory actorProfileDirectory;

    // Aktif departmanlari altindaki aktif destek ekipleriyle birlikte dondurur.
    @Transactional(readOnly = true)
    public List<DepartmentResponse> listActiveDepartments() {
        List<DepartmentEntity> departments = departmentRepository.findByActiveTrueOrderByNameAsc();
        Map<UUID, List<SupportTeamResponse>> teamsByDepartment = teamRepository.findActiveTeamsWithActiveDepartments()
                .stream()
                .collect(Collectors.groupingBy(
                        team -> team.getDepartment().getId(),
                        Collectors.mapping(this::toTeamResponse, Collectors.toList())));

        return departments.stream()
                .map(department -> new DepartmentResponse(
                        department.getId(),
                        department.getCode(),
                        department.getName(),
                        teamsByDepartment.getOrDefault(department.getId(), List.of())))
                .toList();
    }

    // Aktif departmanlara bagli aktif destek ekiplerini listeler.
    @Transactional(readOnly = true)
    public List<SupportTeamResponse> listActiveTeams() {
        return teamRepository.findActiveTeamsWithActiveDepartments()
                .stream()
                .map(this::toTeamResponse)
                .toList();
    }

    // Aktif bir destek ekibinin aktif uyelerini listeler.
    @Transactional(readOnly = true)
    public List<TeamMemberResponse> listActiveTeamMembers(UUID teamId) {
        teamRepository.findActiveTeamWithActiveDepartment(teamId)
                .orElseThrow(() -> NotFoundException.team(teamId));
        return teamMemberRepository.findActiveMembersForActiveTeam(teamId)
                .stream()
                .map(this::toTeamMemberResponse)
                .toList();
    }

    // Actor kimliginin aktif ekip uyeliklerini listeler.
    @Transactional(readOnly = true)
    public List<TeamMemberResponse> listActiveMembershipsForActor(UUID actorId) {
        return teamMemberRepository.findActiveMembershipsForActor(actorId)
                .stream()
                .map(this::toTeamMemberResponse)
                .toList();
    }

    private SupportTeamResponse toTeamResponse(SupportTeamEntity team) {
        DepartmentEntity department = team.getDepartment();
        return new SupportTeamResponse(
                team.getId(),
                department.getId(),
                department.getCode(),
                team.getCode(),
                team.getName(),
                team.getLeadActorId());
    }

    private TeamMemberResponse toTeamMemberResponse(TeamMemberEntity member) {
        SupportTeamEntity team = member.getTeam();
        ActorProfileDirectory.ActorProfile profile = actorProfileDirectory.findByActorId(member.getActorId())
                .orElse(null);
        return new TeamMemberResponse(
                member.getActorId(),
                team.getId(),
                team.getCode(),
                profile == null ? null : profile.displayName(),
                profile == null ? null : profile.email(),
                member.isTeamLead());
    }
}
