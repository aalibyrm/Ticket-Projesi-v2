package com.ticketmanagement.notification.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, UUID> {

    long countBySourceEventId(UUID sourceEventId);
}
