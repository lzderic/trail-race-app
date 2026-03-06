package com.race.query.controller;

import com.race.query.dto.ApplicationResponse;
import com.race.query.service.ApplicationQueryService;
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
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "Applications (Query)", description = "Read race applications — available to all authenticated users")
@SecurityRequirement(name = "bearerAuth")
public class ApplicationQueryController {

    private final ApplicationQueryService applicationQueryService;

    @Operation(summary = "Get all applications", description = "Returns a list of all race applications.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Applications retrieved",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApplicationResponse.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getAllApplications() {
        return ResponseEntity.ok(applicationQueryService.getAllApplications());
    }

    @Operation(summary = "Get an application by ID", description = "Returns a single application by its UUID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Application found",
            content = @Content(schema = @Schema(implementation = ApplicationResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
        @ApiResponse(responseCode = "404", description = "Application not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @Parameter(description = "Application UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(applicationQueryService.getApplicationById(id));
    }
}
