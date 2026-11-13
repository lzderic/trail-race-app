package com.race.query.mapper;

import com.race.query.dto.RaceResponse;
import com.race.query.entity.Race;

/** Converts {@link Race} read-model entities into HTTP response DTOs. */
public final class RaceMapper {

    private RaceMapper() {
    }

    public static RaceResponse toResponse(Race race) {
        return new RaceResponse(race.getId(), race.getName(), race.getDistance());
    }
}
