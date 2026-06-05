package com.ticketmanagement.ticket.application;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.ticketmanagement.ticket.api.dto.ConversationReadStateResponse;
import com.ticketmanagement.ticket.domain.TicketCommentVisibility;
import com.ticketmanagement.ticket.domain.TicketConversationReadScope;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketCommentJpaRepository;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketConversationReadEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketConversationReadJpaRepository;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketConversationReadService {

    private final TicketCommentJpaRepository ticketCommentRepository;
    private final TicketConversationReadJpaRepository ticketConversationReadRepository;
    private final TicketJpaRepository ticketRepository;
    private final TicketSupportAccessService ticketSupportAccessService;

    // Musterinin kendi ticket'indaki external mesaj okunma durumunu dondurur.
    @Transactional(readOnly = true)
    public ConversationReadStateResponse getCustomerReadState(UUID customerId, UUID ticketId) {
        TicketEntity ticket = findCustomerTicket(customerId, ticketId);
        return readState(ticket, customerId, TicketConversationReadScope.CUSTOMER_EXTERNAL, TicketCommentVisibility.EXTERNAL);
    }

    // Musterinin kendi ticket'indaki external mesajlari okundu olarak isaretler.
    @Transactional
    public ConversationReadStateResponse markCustomerConversationRead(UUID customerId, UUID ticketId) {
        TicketEntity ticket = findCustomerTicket(customerId, ticketId);
        return markRead(ticket, customerId, TicketConversationReadScope.CUSTOMER_EXTERNAL, TicketCommentVisibility.EXTERNAL);
    }

    // Support actor'un gorebildigi ticket mesajlari icin okunma durumunu dondurur.
    @Transactional(readOnly = true)
    public ConversationReadStateResponse getSupportReadState(SupportActorContext context, UUID ticketId) {
        TicketEntity ticket = findSupportTicket(context, ticketId);
        return readState(ticket, context.actorId(), TicketConversationReadScope.SUPPORT_ALL, null);
    }

    // Support actor'un gorebildigi ticket mesajlarini okundu olarak isaretler.
    @Transactional
    public ConversationReadStateResponse markSupportConversationRead(SupportActorContext context, UUID ticketId) {
        TicketEntity ticket = findSupportTicket(context, ticketId);
        return markRead(ticket, context.actorId(), TicketConversationReadScope.SUPPORT_ALL, null);
    }

    // Okunma state'ini marker varsa ona gore, yoksa tum uygun diger yorumlari unread sayarak dondurur.
    private ConversationReadStateResponse readState(
            TicketEntity ticket,
            UUID actorId,
            TicketConversationReadScope scope,
            TicketCommentVisibility visibility) {
        OffsetDateTime lastReadAt = ticketConversationReadRepository
                .findByTicketIdAndActorIdAndScope(ticket.getId(), actorId, scope)
                .map(TicketConversationReadEntity::getLastReadAt)
                .orElse(null);
        return new ConversationReadStateResponse(
                ticket.getId(),
                unreadCount(ticket.getId(), actorId, visibility, lastReadAt),
                lastReadAt);
    }

    // Uygun en son yorum zamanina gore okunma marker'ini upsert eder.
    private ConversationReadStateResponse markRead(
            TicketEntity ticket,
            UUID actorId,
            TicketConversationReadScope scope,
            TicketCommentVisibility visibility) {
        OffsetDateTime latestCommentAt = latestCommentAt(ticket.getId(), visibility);
        OffsetDateTime readAt = latestCommentAt == null ? OffsetDateTime.now() : latestCommentAt;
        TicketConversationReadEntity readState = ticketConversationReadRepository
                .findByTicketIdAndActorIdAndScope(ticket.getId(), actorId, scope)
                .orElseGet(() -> TicketConversationReadEntity.create(
                        UUID.randomUUID(),
                        ticket,
                        actorId,
                        scope,
                        readAt));
        readState.markReadAt(readAt);
        ticketConversationReadRepository.save(readState);
        return new ConversationReadStateResponse(ticket.getId(), 0, readAt);
    }

    // Marker zamanindan sonra diger actor'lerin yazdigi yorumlari sayar.
    private long unreadCount(
            UUID ticketId,
            UUID actorId,
            TicketCommentVisibility visibility,
            OffsetDateTime lastReadAt) {
        if (visibility == null) {
            return lastReadAt == null
                    ? ticketCommentRepository.countUnreadAllForActor(ticketId, actorId)
                    : ticketCommentRepository.countUnreadAllForActorSince(ticketId, actorId, lastReadAt);
        }
        return lastReadAt == null
                ? ticketCommentRepository.countUnreadVisibleForActor(ticketId, actorId, visibility)
                : ticketCommentRepository.countUnreadVisibleForActorSince(ticketId, actorId, visibility, lastReadAt);
    }

    // Scope'a gore okunacak en son yorum marker zamanini bulur.
    private OffsetDateTime latestCommentAt(UUID ticketId, TicketCommentVisibility visibility) {
        if (visibility == null) {
            return ticketCommentRepository.findLatestCommentCreatedAt(ticketId);
        }
        return ticketCommentRepository.findLatestVisibleCommentCreatedAt(ticketId, visibility);
    }

    // Musteri icin ticket sahipligini dogrulayarak ticket kaydini getirir.
    private TicketEntity findCustomerTicket(UUID customerId, UUID ticketId) {
        TicketEntity ticket = findTicket(ticketId);
        if (!ticket.getCustomerId().equals(customerId)) {
            throw ForbiddenOperationException.accessDenied();
        }
        return ticket;
    }

    // Support actor icin ticket okuma yetkisini dogrulayarak ticket kaydini getirir.
    private TicketEntity findSupportTicket(SupportActorContext context, UUID ticketId) {
        TicketEntity ticket = findTicket(ticketId);
        ticketSupportAccessService.assertCanReadTicket(ticket, context);
        return ticket;
    }

    // Ticket kaydini getirir veya standart not found hatasi uretir.
    private TicketEntity findTicket(UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> NotFoundException.ticket(ticketId));
    }
}
