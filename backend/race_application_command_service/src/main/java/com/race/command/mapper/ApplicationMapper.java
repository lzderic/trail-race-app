package com.race.command.mapper;

import com.race.command.dto.ApplicationResponse;
import com.race.command.entity.Application;
import com.race.command.event.ApplicationEventPayload;

/** Converts {@link Application} entities into the objects that leave the service: HTTP responses and events. */
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

    public static ApplicationEventPayload toEventPayload(Application application) {
        return new ApplicationEventPayload(
                application.getId(),
                application.getFirstName(),
                application.getLastName(),
                application.getClub(),
                application.getRaceId(),
                application.getUserId()
        );
    }
}
