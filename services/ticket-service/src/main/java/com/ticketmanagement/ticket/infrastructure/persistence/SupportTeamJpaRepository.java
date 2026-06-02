package com.ticketmanagement.ticket.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SupportTeamJpaRepository extends JpaRepository<SupportTeamEntity, UUID> {

    @Query("""
            select team
            from SupportTeamEntity team
            join fetch team.department department
            where team.active = true
              and department.active = true
            order by department.name asc, team.name asc
            """)
    List<SupportTeamEntity> findActiveTeamsWithActiveDepartments();

    @Query("""
            select team
            from SupportTeamEntity team
            join fetch team.department department
            where team.id = :teamId
              and team.active = true
              and department.active = true
            """)
    Optional<SupportTeamEntity> findActiveTeamWithActiveDepartment(UUID teamId);
}
