package com.race.command.event;

import com.race.command.dto.RaceResponse;

public record RaceEvent(EventType eventType, RaceResponse payload) {}

