package com.race.query.mapper;

import com.race.query.dto.ApplicationResponse;
import com.race.query.entity.Application;

/** Converts {@link Application} read-model entities into HTTP response DTOs. */
public final class ApplicationMapper {

    private ApplicationMapper() {
    }

    public static ApplicationResponse toResponse(Application application) {
        return new ApplicationResponse(
                application.getId(),
                application.getFirstName(),
                application.getLastName(),
                application.getClub(),
                application.getRaceId(),
                application.getUserId()
        );
    }
}
