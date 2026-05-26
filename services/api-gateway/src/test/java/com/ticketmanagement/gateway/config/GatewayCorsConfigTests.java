package com.ticketmanagement.gateway.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class GatewayCorsConfigTests {

    @Test
    void acceptsExplicitOriginAllowlist() {
        GatewayCorsConfig config = new GatewayCorsConfig();

        assertThatCode(() -> config.corsWebFilter("http://localhost:5173,https://app.example.com"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsWildcardOriginAllowlist() {
        GatewayCorsConfig config = new GatewayCorsConfig();

        assertThatThrownBy(() -> config.corsWebFilter("*"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Wildcard CORS origins are not allowed");
    }
}
