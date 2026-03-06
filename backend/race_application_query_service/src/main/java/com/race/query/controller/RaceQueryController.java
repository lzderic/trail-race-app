package com.race.query.controller;

import com.race.query.dto.RaceResponse;
import com.race.query.service.RaceQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/races")
@RequiredArgsConstructor
@Tag(name = "Races (Query)", description = "Read races — available to all authenticated users")
@SecurityRequirement(name = "bearerAuth")
public class RaceQueryController {

    private final RaceQueryService raceQueryService;

    @Operation(summary = "Get all races", description = "Returns a list of all available races.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Races retrieved",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = RaceResponse.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<RaceResponse>> getAllRaces() {
        return ResponseEntity.ok(raceQueryService.getAllRaces());
    }

    @Operation(summary = "Get a race by ID", description = "Returns a single race by its UUID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Race found",
            content = @Content(schema = @Schema(implementation = RaceResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
        @ApiResponse(responseCode = "404", description = "Race not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<RaceResponse> getRaceById(
            @Parameter(description = "Race UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(raceQueryService.getRaceById(id));
    }
}
