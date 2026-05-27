package com.ticketmanagement.notification.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticketmanagement.notification.domain.EmailDeliveryStatus;

public interface EmailDeliveryJpaRepository extends JpaRepository<EmailDeliveryEntity, UUID> {

    boolean existsBySourceEventIdAndTemplateKeyAndRecipientEmail(
            UUID sourceEventId,
            String templateKey,
            String recipientEmail);

    Optional<EmailDeliveryEntity> findBySourceEventIdAndTemplateKeyAndRecipientEmail(
            UUID sourceEventId,
            String templateKey,
            String recipientEmail);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select delivery
            from EmailDeliveryEntity delivery
            where delivery.status = :pendingStatus
               or (delivery.status in :scheduledStatuses and delivery.nextAttemptAt <= :now)
            order by delivery.createdAt
            """)
    List<EmailDeliveryEntity> findDueForUpdate(
            @Param("pendingStatus") EmailDeliveryStatus pendingStatus,
            @Param("scheduledStatuses") Collection<EmailDeliveryStatus> scheduledStatuses,
            @Param("now") OffsetDateTime now,
            Pageable pageable);
}
