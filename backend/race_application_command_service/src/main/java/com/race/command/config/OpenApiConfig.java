package com.race.command.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Trail Race — Command Service API")
                        .description("""
                                Write-side of the Trail Race CQRS backend.
                                All mutating endpoints (CREATE / UPDATE / DELETE) for Races and Applications.
                                Requires **Administrator** role.
                                
                                **Login** via `POST /api/auth/login` to obtain a JWT, then click **Authorize** and paste the token.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Trail Race Team")
                                .email("admin@trailrace.com")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the JWT token obtained from POST /api/auth/login")));
    }
}

