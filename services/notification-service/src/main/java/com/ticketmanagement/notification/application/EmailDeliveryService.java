package com.ticketmanagement.notification.application;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.ticketmanagement.notification.config.EmailRetryProperties;
import com.ticketmanagement.notification.domain.EmailDeliveryStatus;
import com.ticketmanagement.notification.domain.EmailTemplateKey;
import com.ticketmanagement.notification.infrastructure.persistence.EmailDeliveryEntity;
import com.ticketmanagement.notification.infrastructure.persistence.EmailDeliveryJpaRepository;

@Service
@RequiredArgsConstructor
public class EmailDeliveryService {

    private static final TypeReference<Map<String, Object>> TEMPLATE_MODEL_TYPE = new TypeReference<>() {
    };
    private static final List<EmailDeliveryStatus> SCHEDULED_RETRY_STATUSES = List.of(
            EmailDeliveryStatus.FAILED,
            EmailDeliveryStatus.RETRYING);

    private final EmailDeliveryJpaRepository emailDeliveryRepository;
    private final EmailTemplateRendererPort templateRenderer;
    private final EmailSenderPort emailSender;
    private final EmailRetryProperties retryProperties;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    // E-posta delivery kaydini event/template/recipient bazinda duplicate uretmeden kuyruga alir.
    public boolean enqueueDelivery(
            UUID sourceEventId,
            String recipientEmail,
            EmailTemplateKey templateKey,
            Map<String, Object> templateModel) {
        Objects.requireNonNull(sourceEventId, "sourceEventId must not be null");
        String normalizedRecipientEmail = normalizeRecipientEmail(recipientEmail);
        EmailTemplateRenderRequest renderRequest = new EmailTemplateRenderRequest(templateKey, templateModel);
        RenderedEmailTemplate renderedTemplate = templateRenderer.render(renderRequest);
        JsonNode sanitizedTemplateModel = objectMapper.valueToTree(renderRequest.model());

        return Boolean.TRUE.equals(transactionTemplate.execute(transactionStatus -> {
            if (emailDeliveryRepository.existsBySourceEventIdAndTemplateKeyAndRecipientEmail(
                    sourceEventId,
                    templateKey.getValue(),
                    normalizedRecipientEmail)) {
                return false;
            }
            try {
                emailDeliveryRepository.saveAndFlush(EmailDeliveryEntity.pending(
                        UUID.randomUUID(),
                        sourceEventId,
                        normalizedRecipientEmail,
                        renderedTemplate.subject(),
                        templateKey.getValue(),
                        sanitizedTemplateModel));
                return true;
            } catch (DataIntegrityViolationException exception) {
                transactionStatus.setRollbackOnly();
                return false;
            }
        }));
    }

    // Due durumdaki e-posta delivery kayitlarini claim edip gondermeyi dener.
    public int processDueDeliveries() {
        List<UUID> claimedDeliveryIds = claimDueDeliveries();
        claimedDeliveryIds.forEach(this::sendClaimedDelivery);
        return claimedDeliveryIds.size();
    }

    // Retry icin uygun kayitlari kisa transaction icinde RETRYING olarak isaretler.
    private List<UUID> claimDueDeliveries() {
        return transactionTemplate.execute(transactionStatus -> {
            OffsetDateTime now = utcNow();
            OffsetDateTime leaseExpiresAt = now.plus(processingLease());
            List<EmailDeliveryEntity> dueDeliveries = emailDeliveryRepository.findDueForUpdate(
                    EmailDeliveryStatus.PENDING,
                    SCHEDULED_RETRY_STATUSES,
                    now,
                    PageRequest.of(0, batchSize()));
            dueDeliveries.forEach(delivery -> delivery.markRetrying(leaseExpiresAt));
            return dueDeliveries.stream()
                    .map(EmailDeliveryEntity::getId)
                    .toList();
        });
    }

    // Claim edilmis delivery kaydini render edip SMTP portuna aktarir.
    private void sendClaimedDelivery(UUID deliveryId) {
        loadClaimedDelivery(deliveryId).ifPresent(command -> {
            try {
                RenderedEmailTemplate renderedTemplate = templateRenderer.render(new EmailTemplateRenderRequest(
                        EmailTemplateKey.fromValue(command.templateKey()),
                        objectMapper.convertValue(command.templateModel(), TEMPLATE_MODEL_TYPE)));
                emailSender.send(new EmailMessage(
                        command.recipientEmail(),
                        renderedTemplate.subject(),
                        renderedTemplate.textBody(),
                        renderedTemplate.htmlBody()));
                markDeliverySent(deliveryId);
            } catch (RuntimeException exception) {
                markDeliveryFailed(deliveryId, safeError(exception));
            }
        });
    }

    // RETRYING durumundaki delivery kaydini transaction disi gonderim komutuna cevirir.
    private Optional<EmailDeliverySendCommand> loadClaimedDelivery(UUID deliveryId) {
        return transactionTemplate.execute(transactionStatus -> emailDeliveryRepository.findById(deliveryId)
                .filter(delivery -> delivery.getStatus() == EmailDeliveryStatus.RETRYING)
                .map(delivery -> new EmailDeliverySendCommand(
                        delivery.getRecipientEmail(),
                        delivery.getTemplateKey(),
                        delivery.getTemplateModel())));
    }

    // Basarili gonderimi SENT olarak kalici hale getirir.
    private void markDeliverySent(UUID deliveryId) {
        transactionTemplate.executeWithoutResult(transactionStatus -> emailDeliveryRepository.findById(deliveryId)
                .ifPresent(delivery -> delivery.markSent(utcNow())));
    }

    // Basarisiz gonderimi retry politikasina gore FAILED olarak planlar.
    private void markDeliveryFailed(UUID deliveryId, String errorMessage) {
        transactionTemplate.executeWithoutResult(transactionStatus -> emailDeliveryRepository.findById(deliveryId)
                .ifPresent(delivery -> delivery.markFailed(errorMessage, nextAttemptAt(delivery))));
    }

    // Basarisiz denemenin tekrar planlanip planlanmayacagini hesaplar.
    private OffsetDateTime nextAttemptAt(EmailDeliveryEntity delivery) {
        if (delivery.getRetryCount() + 1 >= maxAttempts()) {
            return null;
        }
        return utcNow().plus(retryBackoff());
    }

    // Tek seferde claim edilecek kayit sayisini guvenli alt sinirla dondurur.
    private int batchSize() {
        return Math.max(1, retryProperties.getBatchSize());
    }

    // Maksimum deneme sayisini guvenli alt sinirla dondurur.
    private int maxAttempts() {
        return Math.max(1, retryProperties.getMaxAttempts());
    }

    // Negatif veya bos retry backoff degerini guvenli varsayilana cevirir.
    private Duration retryBackoff() {
        Duration configuredBackoff = retryProperties.getBackoff();
        if (configuredBackoff == null || configuredBackoff.isNegative()) {
            return Duration.ZERO;
        }
        return configuredBackoff;
    }

    // RETRYING claim lease suresini guvenli varsayilana cevirir.
    private Duration processingLease() {
        Duration configuredLease = retryProperties.getProcessingLease();
        if (configuredLease == null || configuredLease.isNegative() || configuredLease.isZero()) {
            return Duration.ofMinutes(5);
        }
        return configuredLease;
    }

    // Delivery state zamanlarini UTC olarak uretir.
    private static OffsetDateTime utcNow() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    // Recipient e-posta adresini validate edip case-insensitive dedupe icin normalize eder.
    private static String normalizeRecipientEmail(String recipientEmail) {
        String normalized = requireText(recipientEmail, "recipientEmail").toLowerCase(Locale.ROOT);
        try {
            InternetAddress address = new InternetAddress(normalized);
            address.validate();
            return address.getAddress().toLowerCase(Locale.ROOT);
        } catch (AddressException exception) {
            throw new IllegalArgumentException("recipientEmail must be valid", exception);
        }
    }

    // Zorunlu text alanlarini bos degerlere karsi korur.
    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    // Hata bilgisini tek satir ve loglanabilir guvenli ozete indirger.
    private static String safeError(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return exception.getClass().getSimpleName() + ": " + message.replaceAll("[\\r\\n\\t]+", " ").trim();
    }

    private record EmailDeliverySendCommand(
            String recipientEmail,
            String templateKey,
            JsonNode templateModel) {
    }
}
