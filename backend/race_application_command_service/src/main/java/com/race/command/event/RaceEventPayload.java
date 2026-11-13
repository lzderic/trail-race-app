package com.race.command.event;

import com.race.command.enums.Distance;

import java.util.UUID;

/**
 * Integration-event payload describing a race.
 * <p>
 * This is the contract shared with the query service over RabbitMQ. It is deliberately
 * separate from the HTTP response DTOs so the public API and the messaging contract
 * can evolve independently.
 */
public record RaceEventPayload(UUID id, String name, Distance distance) {}
