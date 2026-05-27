package com.ticketmanagement.workflow.config;

import org.kie.kogito.correlation.CorrelationService;
import org.kie.kogito.event.correlation.DefaultCorrelationService;
import org.kie.kogito.services.uow.StaticUnitOfWorkManger;
import org.kie.kogito.uow.UnitOfWorkManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class KogitoRuntimeConfig {

    @Bean
    CorrelationService correlationService() {
        return new DefaultCorrelationService();
    }

    @Bean
    UnitOfWorkManager unitOfWorkManager() {
        return StaticUnitOfWorkManger.staticUnitOfWorkManager();
    }
}
