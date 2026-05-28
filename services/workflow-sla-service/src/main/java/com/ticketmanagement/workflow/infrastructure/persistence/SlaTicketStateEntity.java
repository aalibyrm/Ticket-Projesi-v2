package com.ticketmanagement.workflow.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.ticketmanagement.workflow.domain.SlaPriority;
import com.ticketmanagement.workflow.domain.SlaStatus;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "sla_ticket_states", schema = "workflow_schema")
public class SlaTicketStateEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID ticketId;

    @Column(nullable = false, length = 32)
    private String ticketNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SlaPriority priority;

    @Column(nullable = false)
    private UUID customerId;

    private UUID assigneeId;

    private UUID assignedTeamId;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime openedAt;

    @Column(nullable = false)
    private OffsetDateTime targetResolutionAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SlaStatus status;

    private OffsetDateTime riskDetectedAt;

    private OffsetDateTime breachedAt;

    @Setter(AccessLevel.NONE)
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    public static SlaTicketStateEntity active(
            UUID ticketId,
            String ticketNumber,
            SlaPriority priority,
            UUID customerId,
            OffsetDateTime openedAt,
            OffsetDateTime targetResolutionAt) {
        SlaTicketStateEntity state = new SlaTicketStateEntity();
        state.ticketId = ticketId;
        state.ticketNumber = ticketNumber;
        state.priority = priority;
        state.customerId = customerId;
        state.openedAt = openedAt;
        state.targetResolutionAt = targetResolutionAt;
        state.status = SlaStatus.ACTIVE;
        return state;
    }

    public void updateAssignment(UUID assigneeId, UUID assignedTeamId) {
        this.assigneeId = assigneeId;
        this.assignedTeamId = assignedTeamId;
    }

    public boolean markAtRisk(OffsetDateTime detectedAt) {
        if (status != SlaStatus.ACTIVE) {
            return false;
        }
        status = SlaStatus.AT_RISK;
        riskDetectedAt = detectedAt;
        return true;
    }

    public boolean markBreached(OffsetDateTime detectedAt) {
        if (status == SlaStatus.BREACHED || status == SlaStatus.MET) {
            return false;
        }
        status = SlaStatus.BREACHED;
        breachedAt = detectedAt;
        return true;
    }

    public UUID alertRecipientId() {
        return assigneeId == null ? customerId : assigneeId;
    }

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
