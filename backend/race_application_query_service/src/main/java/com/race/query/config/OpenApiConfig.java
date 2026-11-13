package com.race.query.config;

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
                        .title("Trail Race — Query Service API")
                        .description("""
                                Read-side of the Trail Race CQRS backend.
                                GET endpoints for Races and Applications, populated via RabbitMQ events.
                                Requires any authenticated user role (**Applicant** or **Administrator**).
                                Races are visible to everyone; applications are visible to their owner and to administrators.
                                Every error is returned as an RFC 9457 Problem Details body.
                                
                                **Login** via `POST /api/auth/login` to obtain a JWT, then click **Authorize** and paste the token.
                                
                                **Seed credentials:**
                                | Email | Password | Role |
                                |---|---|---|
                                | admin@trailrace.com | admin123 | Administrator |
                                | jane@example.com | applicant123 | Applicant |
                                | marco@example.com | applicant123 | Applicant |
                                | lena@example.com | applicant123 | Applicant |
                                
                                **Seed races:** 6 sample races (Mountain Trail 5K, Forest Loop 10K, Coastal Half Marathon, Alpine Marathon, City Park 5K, River Valley 10K) are pre-loaded.
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

