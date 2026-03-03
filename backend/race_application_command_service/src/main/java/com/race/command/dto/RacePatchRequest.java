package com.race.command.dto;

import com.race.command.enums.Distance;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for partially updating a race (all fields optional)")
public record RacePatchRequest(
        @Schema(description = "New race name", example = "Coastal Trail 10K", maxLength = 255)
        String name,

        @Schema(description = "New race distance", example = "TEN_K")
        Distance distance
) {}
