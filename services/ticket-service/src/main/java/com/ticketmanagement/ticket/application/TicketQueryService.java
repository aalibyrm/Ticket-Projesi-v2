package com.ticketmanagement.ticket.application;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.ticket.api.dto.TicketResponse;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketJpaRepository;

@Service
@RequiredArgsConstructor
public class TicketQueryService {

    private final TicketJpaRepository ticketRepository;
    private final TicketMapper ticketMapper;

    // Musterinin kendi ticket listesini en yeni kayitlar once gelecek sekilde getirir.
    @Transactional(readOnly = true)
    public List<TicketResponse> listTicketsForCustomer(UUID customerId) {
        return ticketRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    // Musterinin sadece kendisine ait ticket detayini getirir.
    @Transactional(readOnly = true)
    public TicketResponse getTicketForCustomer(UUID customerId, UUID ticketId) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> NotFoundException.ticket(ticketId));

        if (!ticket.getCustomerId().equals(customerId)) {
            throw ForbiddenOperationException.accessDenied();
        }

        return ticketMapper.toResponse(ticket);
    }
}
