package com.ticketmanagement.file.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class FileServiceClockConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
