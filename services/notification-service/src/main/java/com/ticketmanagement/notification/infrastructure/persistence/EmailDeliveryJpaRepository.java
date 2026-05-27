package com.ticketmanagement.notification.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailDeliveryJpaRepository extends JpaRepository<EmailDeliveryEntity, UUID> {
}
