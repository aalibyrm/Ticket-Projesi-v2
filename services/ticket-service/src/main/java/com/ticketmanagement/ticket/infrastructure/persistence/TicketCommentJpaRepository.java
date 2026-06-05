package com.ticketmanagement.ticket.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticketmanagement.ticket.domain.TicketCommentVisibility;

public interface TicketCommentJpaRepository extends JpaRepository<TicketCommentEntity, UUID> {

    List<TicketCommentEntity> findByTicketIdOrderByCreatedAtAsc(UUID ticketId);

    List<TicketCommentEntity> findByTicketIdAndVisibilityOrderByCreatedAtAsc(
            UUID ticketId,
            TicketCommentVisibility visibility);

    @Query("""
            select count(comment)
            from TicketCommentEntity comment
            where comment.ticket.id = :ticketId
              and comment.authorId <> :actorId
            """)
    long countUnreadAllForActor(
            @Param("ticketId") UUID ticketId,
            @Param("actorId") UUID actorId);

    @Query("""
            select count(comment)
            from TicketCommentEntity comment
            where comment.ticket.id = :ticketId
              and comment.authorId <> :actorId
              and comment.createdAt > :lastReadAt
            """)
    long countUnreadAllForActorSince(
            @Param("ticketId") UUID ticketId,
            @Param("actorId") UUID actorId,
            @Param("lastReadAt") OffsetDateTime lastReadAt);

    @Query("""
            select count(comment)
            from TicketCommentEntity comment
            where comment.ticket.id = :ticketId
              and comment.authorId <> :actorId
              and comment.visibility = :visibility
            """)
    long countUnreadVisibleForActor(
            @Param("ticketId") UUID ticketId,
            @Param("actorId") UUID actorId,
            @Param("visibility") TicketCommentVisibility visibility);

    @Query("""
            select count(comment)
            from TicketCommentEntity comment
            where comment.ticket.id = :ticketId
              and comment.authorId <> :actorId
              and comment.visibility = :visibility
              and comment.createdAt > :lastReadAt
            """)
    long countUnreadVisibleForActorSince(
            @Param("ticketId") UUID ticketId,
            @Param("actorId") UUID actorId,
            @Param("visibility") TicketCommentVisibility visibility,
            @Param("lastReadAt") OffsetDateTime lastReadAt);

    @Query("""
            select max(comment.createdAt)
            from TicketCommentEntity comment
            where comment.ticket.id = :ticketId
            """)
    OffsetDateTime findLatestCommentCreatedAt(@Param("ticketId") UUID ticketId);

    @Query("""
            select max(comment.createdAt)
            from TicketCommentEntity comment
            where comment.ticket.id = :ticketId
              and comment.visibility = :visibility
            """)
    OffsetDateTime findLatestVisibleCommentCreatedAt(
            @Param("ticketId") UUID ticketId,
            @Param("visibility") TicketCommentVisibility visibility);
}
