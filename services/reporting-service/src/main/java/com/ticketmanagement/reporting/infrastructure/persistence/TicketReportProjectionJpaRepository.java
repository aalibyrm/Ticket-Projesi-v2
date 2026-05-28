package com.ticketmanagement.reporting.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
