package com.ticketmanagement.ticket.application;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.ticket.api.dto.AddExternalCommentRequest;
import com.ticketmanagement.ticket.api.dto.CreateTicketRequest;
import com.ticketmanagement.ticket.api.dto.TicketCommentResponse;
import com.ticketmanagement.ticket.api.dto.TicketResponse;
import com.ticketmanagement.ticket.domain.TicketPriority;
import com.ticketmanagement.ticket.infrastructure.persistence.ProductEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.ProductJpaRepository;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketCommentEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketCommentJpaRepository;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketJpaRepository;

@Service
@RequiredArgsConstructor
public class TicketCommandService {

    private final ProductJpaRepository productRepository;
    private final TicketCommentJpaRepository ticketCommentRepository;
    private final TicketJpaRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final TicketNumberGenerator ticketNumberGenerator;
    private final TicketOutboxService ticketOutboxService;
    private final TicketRoutingService ticketRoutingService;

    // Musteri adina yeni ticket kaydi olusturur.
    @Transactional
    public TicketResponse createTicket(UUID customerId, CreateTicketRequest request) {
        ProductEntity product = productRepository.findByIdAndActiveTrue(request.productId())
                .orElseThrow(() -> NotFoundException.product(request.productId()));
        TicketRoutingResolution routing = ticketRoutingService.resolveActiveRoute(request.topicCode());

        TicketEntity ticket = TicketEntity.open(
                UUID.randomUUID(),
                ticketNumberGenerator.nextTicketNumber(),
                customerId,
                product,
                routing.topic(),
                routing.department(),
                routing.team().getId(),
                request.summary().trim(),
                request.description().trim(),
                request.priority() == null ? TicketPriority.MEDIUM : request.priority());

        TicketEntity savedTicket = ticketRepository.save(ticket);
        ticketOutboxService.saveTicketCreated(savedTicket, customerId);
        ticketOutboxService.saveTicketAssigned(savedTicket, customerId);

        return ticketMapper.toResponse(savedTicket);
    }

    // Musterinin sadece kendi ticket'ina external yorum eklemesini saglar.
    @Transactional
    public TicketCommentResponse addCustomerExternalComment(
            UUID customerId,
            UUID ticketId,
            AddExternalCommentRequest request) {
        TicketEntity ticket = ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() -> NotFoundException.ticket(ticketId));

        if (!ticket.getCustomerId().equals(customerId)) {
            throw ForbiddenOperationException.accessDenied();
        }

        TicketCommentEntity comment = ticketCommentRepository.save(TicketCommentEntity.external(
                UUID.randomUUID(),
                ticket,
                customerId,
                request.body().trim()));

        ticketOutboxService.saveExternalCommentAdded(comment, customerId);
        return ticketMapper.toResponse(comment);
    }
}
