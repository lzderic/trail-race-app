package com.race.query.event;

import com.race.query.enums.Distance;

import java.util.UUID;

/**
 * Integration-event payload describing a race, as published by the command service.
 * Kept separate from the HTTP response DTOs so the messaging contract and the public API
 * can evolve independently.
 */
public record RaceEventPayload(UUID id, String name, Distance distance) {}
