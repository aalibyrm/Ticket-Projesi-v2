package com.ticketmanagement.notification.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, UUID> {

    long countBySourceEventId(UUID sourceEventId);

    List<NotificationEntity> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

    List<NotificationEntity> findByRecipientIdAndReadOrderByCreatedAtDesc(UUID recipientId, boolean read);

    Optional<NotificationEntity> findByIdAndRecipientId(UUID id, UUID recipientId);
}
