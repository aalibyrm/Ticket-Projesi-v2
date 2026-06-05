package com.ticketmanagement.ticket.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.ticketmanagement.ticket.domain.TicketConversationReadScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "ticket_conversation_reads",
        schema = "ticket_schema",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ticket_conversation_reads_actor_scope",
                columnNames = {"ticket_id", "actor_id", "scope"}))
public class TicketConversationReadEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketEntity ticket;

    @Column(nullable = false)
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TicketConversationReadScope scope;

    @Column(nullable = false)
    private OffsetDateTime lastReadAt;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    public static TicketConversationReadEntity create(
            UUID id,
            TicketEntity ticket,
            UUID actorId,
            TicketConversationReadScope scope,
            OffsetDateTime lastReadAt) {
        TicketConversationReadEntity readState = new TicketConversationReadEntity();
        readState.id = id;
        readState.ticket = ticket;
        readState.actorId = actorId;
        readState.scope = scope;
        readState.lastReadAt = lastReadAt;
        return readState;
    }

    public void markReadAt(OffsetDateTime readAt) {
        this.lastReadAt = readAt;
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
