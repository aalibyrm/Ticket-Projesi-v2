package com.ticketmanagement.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.security.jwt.enabled=false")
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
    }
}

