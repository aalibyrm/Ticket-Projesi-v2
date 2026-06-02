package com.ticketmanagement.ticket.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.ticket.api.dto.TicketTopicResponse;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketTopicEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketTopicJpaRepository;

@Service
@RequiredArgsConstructor
public class TicketTopicQueryService {

    private final TicketTopicJpaRepository ticketTopicRepository;

    // Musterinin ticket acarken secebilecegi aktif topic katalogunu dondurur.
    @Transactional(readOnly = true)
    public List<TicketTopicResponse> listActiveTopics() {
        return ticketTopicRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private TicketTopicResponse toResponse(TicketTopicEntity topic) {
        return new TicketTopicResponse(
                topic.getId(),
                topic.getCode(),
                topic.getName(),
                topic.getDescription());
    }
}
