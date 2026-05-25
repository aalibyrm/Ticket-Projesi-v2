package com.ticketmanagement.ticket.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ticketmanagement.ticket.api.dto.CreateTicketRequest;
import com.ticketmanagement.ticket.api.dto.TicketResponse;
import com.ticketmanagement.ticket.application.TicketCommandService;
import com.ticketmanagement.ticket.application.TicketQueryService;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
class TicketController {

    private final TicketCommandService ticketCommandService;
    private final TicketQueryService ticketQueryService;

    // Musterinin yeni bir ticket acmasini saglar.
    @PostMapping
    ResponseEntity<TicketResponse> createTicket(
            @RequestHeader("X-Actor-Id") UUID customerId,
            @Valid @RequestBody CreateTicketRequest request) {
        TicketResponse response = ticketCommandService.createTicket(customerId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    // Musterinin kendi ticket listesini dondurur.
    @GetMapping
    List<TicketResponse> listOwnTickets(@RequestHeader("X-Actor-Id") UUID customerId) {
        return ticketQueryService.listTicketsForCustomer(customerId);
    }

    // Musterinin kendisine ait tek bir ticket detayini dondurur.
    @GetMapping("/{id}")
    TicketResponse getOwnTicket(@RequestHeader("X-Actor-Id") UUID customerId, @PathVariable UUID id) {
        return ticketQueryService.getTicketForCustomer(customerId, id);
    }
}

