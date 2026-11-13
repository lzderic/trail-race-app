package com.race.query.controller;

import com.race.query.dto.ApplicationResponse;
import com.race.query.security.AuthenticatedUser;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "Applications (Query)", description = "Read race applications — administrators see all, applicants see their own")
@SecurityRequirement(name = "bearerAuth")
public class ApplicationQueryController {

    private final ApplicationQueryService applicationQueryService;

    @Operation(summary = "List applications",
            description = "Administrators receive every application; applicants receive only the applications they submitted.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Applications retrieved",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApplicationResponse.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthenticated",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getApplications(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(applicationQueryService.getApplicationsVisibleTo(currentUser));
    }

    @Operation(summary = "Get an application by ID",
            description = "Returns a single application. Applicants may only read their own applications.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Application found",
            content = @Content(schema = @Schema(implementation = ApplicationResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthenticated",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — not the owner of the application",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Application not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @Parameter(description = "Application UUID") @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(applicationQueryService.getApplicationById(id, currentUser));
    }
}
