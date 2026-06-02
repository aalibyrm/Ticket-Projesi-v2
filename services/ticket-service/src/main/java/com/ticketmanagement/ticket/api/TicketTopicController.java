package com.ticketmanagement.ticket.api;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketmanagement.ticket.api.dto.TicketTopicResponse;
import com.ticketmanagement.ticket.application.TicketTopicQueryService;

@RestController
@RequestMapping("/api/ticket-topics")
@RequiredArgsConstructor
class TicketTopicController {

    private final TicketTopicQueryService ticketTopicQueryService;

    // Ticket acma formu icin aktif topic katalogunu dondurur.
    @GetMapping
    List<TicketTopicResponse> listTicketTopics() {
        return ticketTopicQueryService.listActiveTopics();
    }
}
