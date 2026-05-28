package com.ticketmanagement.reporting.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.ticketmanagement.reporting.application.TicketProjectionUpsertCommand;
import com.ticketmanagement.reporting.domain.ProjectionPriority;
import com.ticketmanagement.reporting.domain.ProjectionSlaStatus;
import com.ticketmanagement.reporting.domain.ProjectionTicketStatus;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "ticket_report_projection", schema = "reporting_schema")
public class TicketReportProjectionEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID ticketId;

    @Column(nullable = false, length = 32)
    private String ticketNumber;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectionPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ProjectionTicketStatus status;

    private UUID assigneeId;

    private UUID assignedTeamId;

    @Column(nullable = false)
    private OffsetDateTime openedAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    private OffsetDateTime closedAt;

    private OffsetDateTime slaTargetResolutionAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private ProjectionSlaStatus slaStatus;

    @Column(nullable = false)
    private OffsetDateTime projectedAt;

    public static TicketReportProjectionEntity from(TicketProjectionUpsertCommand command) {
        TicketReportProjectionEntity projection = new TicketReportProjectionEntity();
        projection.ticketId = Objects.requireNonNull(command.ticketId(), "ticketId must not be null");
        projection.apply(command);
        return projection;
    }

    public void apply(TicketProjectionUpsertCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        if (ticketId != null && !ticketId.equals(command.ticketId())) {
            throw new IllegalArgumentException("projection ticketId cannot change");
        }

        ticketNumber = command.ticketNumber().trim();
        customerId = command.customerId();
        productId = command.productId();
        priority = command.priority();
        status = command.status();
        assigneeId = command.assigneeId();
        assignedTeamId = command.assignedTeamId();
        openedAt = utc(command.openedAt());
        updatedAt = utc(command.updatedAt());
        closedAt = utcOrNull(command.closedAt());
        slaTargetResolutionAt = utcOrNull(command.slaTargetResolutionAt());
        slaStatus = command.slaStatus();
        projectedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void updateAssignment(UUID assigneeId, UUID assignedTeamId, OffsetDateTime updatedAt) {
        this.assigneeId = Objects.requireNonNull(assigneeId, "assigneeId must not be null");
        this.assignedTeamId = assignedTeamId;
        this.updatedAt = utc(updatedAt);
        projectedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void updateStatus(ProjectionTicketStatus status, OffsetDateTime updatedAt) {
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.updatedAt = utc(updatedAt);
        if (status == ProjectionTicketStatus.CLOSED) {
            closedAt = utc(updatedAt);
        }
        projectedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void updateSla(ProjectionSlaStatus slaStatus, OffsetDateTime slaTargetResolutionAt, OffsetDateTime updatedAt) {
        this.slaStatus = Objects.requireNonNull(slaStatus, "slaStatus must not be null");
        this.slaTargetResolutionAt = utcOrNull(slaTargetResolutionAt);
        this.updatedAt = utc(updatedAt);
        projectedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    private static OffsetDateTime utc(OffsetDateTime value) {
        return Objects.requireNonNull(value, "time value must not be null").withOffsetSameInstant(ZoneOffset.UTC);
    }

    private static OffsetDateTime utcOrNull(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        return value.withOffsetSameInstant(ZoneOffset.UTC);
    }
}
