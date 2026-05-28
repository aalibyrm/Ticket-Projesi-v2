package com.ticketmanagement.reporting.infrastructure.persistence;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.ticketmanagement.reporting.application.AgentWorklogProjectionCommand;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "agent_worklog_projection", schema = "reporting_schema")
public class AgentWorklogProjectionEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID worklogId;

    @Column(nullable = false)
    private UUID ticketId;

    @Column(nullable = false, length = 32)
    private String ticketNumber;

    @Column(nullable = false)
    private UUID agentId;

    @Column(nullable = false)
    private LocalDate workDate;

    @Column(nullable = false)
    private int durationMinutes;

    @Column(nullable = false)
    private OffsetDateTime projectedAt;

    public static AgentWorklogProjectionEntity from(AgentWorklogProjectionCommand command) {
        AgentWorklogProjectionEntity projection = new AgentWorklogProjectionEntity();
        projection.worklogId = Objects.requireNonNull(command.worklogId(), "worklogId must not be null");
        projection.apply(command);
        return projection;
    }

    public void apply(AgentWorklogProjectionCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        if (worklogId != null && !worklogId.equals(command.worklogId())) {
            throw new IllegalArgumentException("projection worklogId cannot change");
        }

        ticketId = command.ticketId();
        ticketNumber = command.ticketNumber().trim();
        agentId = command.agentId();
        workDate = command.workDate();
        durationMinutes = command.durationMinutes();
        projectedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
