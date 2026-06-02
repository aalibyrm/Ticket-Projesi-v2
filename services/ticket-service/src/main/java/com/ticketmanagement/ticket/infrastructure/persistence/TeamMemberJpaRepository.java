package com.ticketmanagement.ticket.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeamMemberJpaRepository extends JpaRepository<TeamMemberEntity, UUID> {

    @Query("""
            select member
            from TeamMemberEntity member
            join fetch member.team team
            join fetch team.department department
            where member.active = true
              and team.id = :teamId
              and team.active = true
              and department.active = true
            order by member.teamLead desc, member.actorId asc
            """)
    List<TeamMemberEntity> findActiveMembersForActiveTeam(UUID teamId);

    @Query("""
            select member
            from TeamMemberEntity member
            join fetch member.team team
            join fetch team.department department
            where member.active = true
              and member.actorId = :actorId
              and team.active = true
              and department.active = true
            order by team.name asc
            """)
    List<TeamMemberEntity> findActiveMembershipsForActor(UUID actorId);

    @Query("""
            select count(member) > 0
            from TeamMemberEntity member
            join member.team team
            join team.department department
            where member.active = true
              and member.actorId = :actorId
              and team.id = :teamId
              and team.active = true
              and department.active = true
            """)
    boolean existsActiveMembership(UUID actorId, UUID teamId);
}
