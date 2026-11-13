package com.race.command.dto;

import com.race.command.enums.Distance;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for partially updating a race (all fields optional)")
public record RacePatchRequest(
        @Schema(description = "New race name", example = "Coastal Trail 10K", maxLength = 255)
        @Size(max = 255)
        @Pattern(regexp = "\\s*\\S[\\s\\S]*", message = "must not be blank")
        String name,

        @Schema(description = "New race distance", example = "10k",
                allowableValues = {"5k", "10k", "HalfMarathon", "Marathon"})
        Distance distance
) {}
