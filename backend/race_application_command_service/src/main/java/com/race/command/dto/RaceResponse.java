package com.race.command.dto;

import com.race.command.enums.Distance;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Race details")
public record RaceResponse(
        @Schema(description = "Race UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Race name", example = "Mountain Classic")
        String name,

        @Schema(description = "Race distance", example = "MARATHON")
        Distance distance
) {}
