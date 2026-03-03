package com.race.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credentials for authentication")
public record LoginRequest(
        @Schema(description = "User email address", example = "admin@trailrace.com")
        @NotBlank @Email String email,

        @Schema(description = "User password", example = "admin123")
        @NotBlank String password
) {}
