package com.race.command.mapper;

import com.race.command.dto.RaceResponse;
import com.race.command.entity.Race;
import com.race.command.event.RaceEventPayload;

/** Converts {@link Race} entities into the objects that leave the service: HTTP responses and events. */
public final class RaceMapper {

    private RaceMapper() {
    }

    public static RaceResponse toResponse(Race race) {
        return new RaceResponse(race.getId(), race.getName(), race.getDistance());
    }

    public static RaceEventPayload toEventPayload(Race race) {
        return new RaceEventPayload(race.getId(), race.getName(), race.getDistance());
    }
}
