package com.race.command.event;

import java.util.UUID;

/**
 * Integration-event payload describing a race application.
 * <p>
 * This is the contract shared with the query service over RabbitMQ. It is deliberately
 * separate from the HTTP response DTOs so the public API and the messaging contract
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
