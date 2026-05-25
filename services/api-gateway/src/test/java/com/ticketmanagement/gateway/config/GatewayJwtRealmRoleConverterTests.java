package com.ticketmanagement.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class GatewayJwtRealmRoleConverterTests {

    @Test
    void convertsKeycloakRealmRolesToSpringAuthorities() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "none"),
                Map.of("realm_access", Map.of("roles", List.of("ADMIN", "CUSTOMER"))));

        assertThat(new GatewayJwtRealmRoleConverter().convert(jwt))
                .extracting(authority -> authority.getAuthority())
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_CUSTOMER");
    }
}

