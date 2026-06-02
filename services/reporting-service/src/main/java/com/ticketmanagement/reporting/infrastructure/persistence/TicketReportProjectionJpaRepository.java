package com.ticketmanagement.reporting.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticketmanagement.reporting.application.DepartmentTicketCount;
import com.ticketmanagement.reporting.application.TeamTicketCount;
import com.ticketmanagement.reporting.application.TicketStatusCount;
import com.ticketmanagement.reporting.domain.ProjectionTicketStatus;

public interface TicketReportProjectionJpaRepository extends JpaRepository<TicketReportProjectionEntity, UUID> {

    @Query("""
            select new com.ticketmanagement.reporting.application.TicketStatusCount(
                projection.status,
                count(projection)
            )
            from TicketReportProjectionEntity projection
            where projection.status <> :closedStatus
            group by projection.status
            """)
    List<TicketStatusCount> countOpenTicketsByStatus(@Param("closedStatus") ProjectionTicketStatus closedStatus);

    @Query("""
            select new com.ticketmanagement.reporting.application.DepartmentTicketCount(
                projection.routedDepartmentId,
                projection.routedDepartmentCode,
                projection.routedDepartmentName,
                count(projection)
            )
            from TicketReportProjectionEntity projection
            where projection.status <> :closedStatus
              and projection.routedDepartmentId is not null
            group by projection.routedDepartmentId, projection.routedDepartmentCode, projection.routedDepartmentName
            order by count(projection) desc
            """)
    List<DepartmentTicketCount> countOpenTicketsByRoutedDepartment(
            @Param("closedStatus") ProjectionTicketStatus closedStatus);

    @Query("""
            select new com.ticketmanagement.reporting.application.TeamTicketCount(
                projection.assignedTeamId,
                count(projection)
            )
            from TicketReportProjectionEntity projection
            where projection.status <> :closedStatus
              and projection.assignedTeamId is not null
            group by projection.assignedTeamId
            order by count(projection) desc
            """)
    List<TeamTicketCount> countOpenTicketsByAssignedTeam(@Param("closedStatus") ProjectionTicketStatus closedStatus);
}
