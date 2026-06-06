package com.ticketmanagement.file.config;

import java.util.List;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfig {

    private static final String API_PREFIX = "/api";
    private static final String PUBLIC_API_PREFIX = "/api/v1";
    private static final String BEARER_JWT = "bearer-jwt";

    @Bean
    OpenAPI fileServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("File Service API")
                        .version("v1")
                        .description("Attachment metadata, upload URL, completion, and download URL API."))
                .servers(List.of(new Server().url("/").description("API Gateway")))
                .components(new Components().addSecuritySchemes(BEARER_JWT, bearerJwtScheme()))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_JWT));
    }

    @Bean
    OpenApiCustomizer publicGatewayPathCustomizer() {
        return openApi -> openApi.setPaths(versionedPublicPaths(openApi.getPaths()));
    }

    private static Paths versionedPublicPaths(Paths source) {
        Paths target = new Paths();
        if (source == null) {
            return target;
        }

        source.forEach((path, item) -> target.addPathItem(publicPath(path), item));
        return target;
    }

    private static String publicPath(String path) {
        if (path.startsWith(API_PREFIX + "/")) {
            return PUBLIC_API_PREFIX + path.substring(API_PREFIX.length());
        }
        return path;
    }

    private static SecurityScheme bearerJwtScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
    }
}
