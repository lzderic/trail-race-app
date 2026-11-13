package com.race.query.event;

import java.util.UUID;

/**
 * Integration-event payload describing a race application, as published by the command service.
 * Kept separate from the HTTP response DTOs so the messaging contract and the public API
 * can evolve independently.
 */
public record ApplicationEventPayload(
        UUID id,
        String firstName,
        String lastName,
        String club,
        UUID raceId,
        UUID userId
) {}
