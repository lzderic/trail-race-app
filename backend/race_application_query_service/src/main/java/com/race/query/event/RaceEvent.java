package com.race.query.event;

import com.race.query.dto.RaceResponse;

public record RaceEvent(EventType eventType, RaceResponse payload) {}

