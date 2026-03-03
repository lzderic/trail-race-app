package com.race.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT authentication response")
public record LoginResponse(
        @Schema(description = "Bearer token to use in Authorization header", example = "eyJhbGciOiJIUzI1NiJ9...")
        String token
) {}
