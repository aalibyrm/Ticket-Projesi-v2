package com.ticketmanagement.ticket.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.ticketmanagement.ticket.domain.TicketCommentVisibility;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "ticket_comments", schema = "ticket_schema")
public class TicketCommentEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketEntity ticket;

    @Column(nullable = false, updatable = false)
    private UUID authorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketCommentVisibility visibility;

    @Column(nullable = false, length = 5000)
    private String body;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public static TicketCommentEntity external(UUID id, TicketEntity ticket, UUID authorId, String body) {
        TicketCommentEntity comment = new TicketCommentEntity();
        comment.id = id;
        comment.ticket = ticket;
        comment.authorId = authorId;
        comment.visibility = TicketCommentVisibility.EXTERNAL;
        comment.body = body;
        return comment;
    }

    public static TicketCommentEntity internal(UUID id, TicketEntity ticket, UUID authorId, String body) {
        TicketCommentEntity comment = new TicketCommentEntity();
        comment.id = id;
        comment.ticket = ticket;
        comment.authorId = authorId;
        comment.visibility = TicketCommentVisibility.INTERNAL;
        comment.body = body;
        return comment;
    }

    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now();
    }
}
