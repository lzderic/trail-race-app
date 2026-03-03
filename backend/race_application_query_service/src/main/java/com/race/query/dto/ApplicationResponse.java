package com.race.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Race application details")
public record ApplicationResponse(
        @Schema(description = "Application UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Participant first name", example = "John")
        String firstName,

        @Schema(description = "Participant last name", example = "Doe")
        String lastName,

        @Schema(description = "Running club (optional)", example = "Trail Blazers")
        String club,

        @Schema(description = "Race UUID this application belongs to", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID raceId
) {}
