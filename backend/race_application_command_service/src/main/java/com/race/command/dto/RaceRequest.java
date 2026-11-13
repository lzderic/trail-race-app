package com.race.command.dto;

import com.race.command.enums.Distance;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for creating a race")
public record RaceRequest(
        @Schema(description = "Race name", example = "Mountain Classic", maxLength = 255)
        @NotBlank @Size(max = 255) String name,

        @Schema(description = "Race distance", example = "Marathon",
                allowableValues = {"5k", "10k", "HalfMarathon", "Marathon"})
        @NotNull Distance distance
) {}
