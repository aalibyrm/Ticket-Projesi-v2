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

    @Column(nullable = false, updatable = false)
    private OffsetDateTime openedAt;

    @Column(nullable = false)
    private OffsetDateTime targetResolutionAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SlaStatus status;

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
            OffsetDateTime openedAt,
            OffsetDateTime targetResolutionAt) {
        SlaTicketStateEntity state = new SlaTicketStateEntity();
        state.ticketId = ticketId;
        state.ticketNumber = ticketNumber;
        state.priority = priority;
        state.openedAt = openedAt;
        state.targetResolutionAt = targetResolutionAt;
        state.status = SlaStatus.ACTIVE;
        return state;
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
