package com.race.command.controller;

import com.race.command.dto.RacePatchRequest;
import com.race.command.dto.RaceRequest;
import com.race.command.dto.RaceResponse;
import com.race.command.service.RaceCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/races")
@RequiredArgsConstructor
@Tag(name = "Races (Command)", description = "Create, update and delete races — Administrator only")
@SecurityRequirement(name = "bearerAuth")
public class RaceCommandController {

    private final RaceCommandService raceCommandService;

    @Operation(summary = "Create a race", description = "Creates a new race. Publishes a CREATED event to the message broker.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Race created",
            content = @Content(schema = @Schema(implementation = RaceResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden — Administrator role required", content = @Content)
    })
    @PostMapping
    public ResponseEntity<RaceResponse> createRace(@Valid @RequestBody RaceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(raceCommandService.createRace(request));
    }

    @Operation(summary = "Update a race", description = "Partially updates a race. Only provided fields are changed. Publishes an UPDATED event.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Race updated",
            content = @Content(schema = @Schema(implementation = RaceResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden — Administrator role required", content = @Content),
        @ApiResponse(responseCode = "404", description = "Race not found", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<RaceResponse> patchRace(
            @Parameter(description = "Race UUID") @PathVariable UUID id,
            @Valid @RequestBody RacePatchRequest request) {
        return ResponseEntity.ok(raceCommandService.patchRace(id, request));
    }

    @Operation(summary = "Delete a race", description = "Deletes a race by ID. Publishes a DELETED event.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Race deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden — Administrator role required", content = @Content),
        @ApiResponse(responseCode = "404", description = "Race not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRace(
            @Parameter(description = "Race UUID") @PathVariable UUID id) {
        raceCommandService.deleteRace(id);
        return ResponseEntity.noContent().build();
    }
}
