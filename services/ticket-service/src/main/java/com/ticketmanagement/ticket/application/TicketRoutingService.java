package com.ticketmanagement.ticket.application;

import java.util.Locale;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.ticket.infrastructure.persistence.DepartmentEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.SupportTeamEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketRoutingRuleEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketRoutingRuleJpaRepository;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketTopicEntity;

@Service
@RequiredArgsConstructor
class TicketRoutingService {

    private final TicketRoutingRuleJpaRepository ticketRoutingRuleRepository;

    // Ticket topic code degerini aktif routing rule uzerinden department ve ekibe cozer.
    @Transactional(readOnly = true)
    TicketRoutingResolution resolveActiveRoute(String topicCode) {
        String normalizedTopicCode = normalizeTopicCode(topicCode);
        TicketRoutingRuleEntity rule = ticketRoutingRuleRepository.findActiveRouteForTopicCode(normalizedTopicCode)
                .orElseThrow(() -> NotFoundException.topic(normalizedTopicCode));
        return new TicketRoutingResolution(rule.getTopic(), rule.getDepartment(), rule.getTeam());
    }

    private String normalizeTopicCode(String topicCode) {
        if (topicCode == null || topicCode.isBlank()) {
            return "";
        }
        return topicCode.trim().toUpperCase(Locale.ROOT);
    }
}

record TicketRoutingResolution(
        TicketTopicEntity topic,
        DepartmentEntity department,
        SupportTeamEntity team) {
}
