package com.race.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Request body for creating a race application")
public record ApplicationRequest(
        @Schema(description = "Participant first name", example = "John", maxLength = 255)
        @NotBlank @Size(max = 255) String firstName,

        @Schema(description = "Participant last name", example = "Doe", maxLength = 255)
        @NotBlank @Size(max = 255) String lastName,

        @Schema(description = "Running club name (optional)", example = "Trail Blazers", maxLength = 255)
        @Size(max = 255) String club,

        @Schema(description = "UUID of the race to apply to", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull UUID raceId
) {}
