package com.ticketmanagement.notification.application;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ticketmanagement.notification.config.EmailRetryProperties;

@Service
@RequiredArgsConstructor
public class EmailDeliveryRetryScheduler {

    private final EmailRetryProperties retryProperties;
    private final EmailDeliveryService emailDeliveryService;

    // Zamanlanmis DB tabanli e-posta retry dongusunu calistirir.
    @Scheduled(fixedDelayString = "${app.email.delivery.retry.fixed-delay-ms:30000}")
    public void retryDueEmailDeliveries() {
        if (retryProperties.isEnabled()) {
            emailDeliveryService.processDueDeliveries();
        }
    }
}
