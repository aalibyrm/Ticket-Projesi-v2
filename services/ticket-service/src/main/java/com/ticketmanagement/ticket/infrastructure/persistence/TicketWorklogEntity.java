package com.ticketmanagement.ticket.infrastructure.persistence;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "ticket_worklogs", schema = "ticket_schema")
public class TicketWorklogEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketEntity ticket;

    @Column(nullable = false, updatable = false)
    private UUID agentId;

    @Column(nullable = false)
    private LocalDate workDate;

    @Column(nullable = false)
    private int durationMinutes;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public static TicketWorklogEntity create(
            UUID id,
            TicketEntity ticket,
            UUID agentId,
            LocalDate workDate,
            int durationMinutes,
            String description) {
        TicketWorklogEntity worklog = new TicketWorklogEntity();
        worklog.id = id;
        worklog.ticket = ticket;
        worklog.agentId = agentId;
        worklog.workDate = workDate;
        worklog.durationMinutes = durationMinutes;
        worklog.description = description;
        return worklog;
    }

    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now();
    }
}
