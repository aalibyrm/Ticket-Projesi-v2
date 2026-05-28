package com.ticketmanagement.workflow.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.workflow.infrastructure.persistence.SlaTicketStateEntity;
import com.ticketmanagement.workflow.infrastructure.persistence.SlaTicketStateJpaRepository;

@Service
@RequiredArgsConstructor
public class SlaTicketStateService {

    private final SlaPolicyService slaPolicyService;
    private final SlaTicketStateJpaRepository slaTicketStateRepository;

    // Ticket acilis eventinden SLA state kaydini olusturur veya mevcut kaydi dondurur.
    @Transactional
    public SlaTicketStateEntity createStateForTicketCreated(TicketSlaCreationCommand command) {
        return slaTicketStateRepository.findById(command.ticketId())
                .orElseGet(() -> slaTicketStateRepository.save(SlaTicketStateEntity.active(
                        command.ticketId(),
                        command.ticketNumber(),
                        command.priority(),
                        command.openedAt(),
                        slaPolicyService.targetResolutionAt(command.priority(), command.openedAt()))));
    }
}
