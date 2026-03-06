package com.race.query.listener;

import com.race.query.dto.ApplicationResponse;
import com.race.query.dto.RaceResponse;
import com.race.query.enums.Distance;
import com.race.query.event.ApplicationEvent;
import com.race.query.event.EventType;
import com.race.query.event.RaceEvent;
import com.race.query.service.ApplicationQueryService;
import com.race.query.service.RaceQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventListenerTest {

    @Mock private RaceQueryService raceQueryService;
    @Mock private ApplicationQueryService applicationQueryService;
    @InjectMocks private EventListener eventListener;

    @Test
    void handleRaceEvent_created_shouldUpsert() {
        UUID id = UUID.randomUUID();
        RaceResponse payload = new RaceResponse(id, "Trail 5K", Distance.FIVE_K);
        RaceEvent event = new RaceEvent(EventType.CREATED, payload);

        eventListener.handleRaceEvent(event);

        verify(raceQueryService).upsertRace(id, "Trail 5K", Distance.FIVE_K);
        verify(raceQueryService, never()).deleteRace(any());
    }

    @Test
    void handleRaceEvent_updated_shouldUpsert() {
        UUID id = UUID.randomUUID();
        RaceResponse payload = new RaceResponse(id, "Updated Race", Distance.TEN_K);
        RaceEvent event = new RaceEvent(EventType.UPDATED, payload);

        eventListener.handleRaceEvent(event);

        verify(raceQueryService).upsertRace(id, "Updated Race", Distance.TEN_K);
        verify(raceQueryService, never()).deleteRace(any());
    }

    @Test
    void handleRaceEvent_deleted_shouldDelete() {
        UUID id = UUID.randomUUID();
        RaceResponse payload = new RaceResponse(id, "Old Race", Distance.MARATHON);
        RaceEvent event = new RaceEvent(EventType.DELETED, payload);

        eventListener.handleRaceEvent(event);

        verify(raceQueryService).deleteRace(id);
        verify(raceQueryService, never()).upsertRace(any(), any(), any());
    }

    @Test
    void handleApplicationEvent_created_shouldUpsert() {
        UUID id = UUID.randomUUID();
        UUID raceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ApplicationResponse payload = new ApplicationResponse(id, "John", "Doe", "Club", raceId, userId);
        ApplicationEvent event = new ApplicationEvent(EventType.CREATED, payload);

        eventListener.handleApplicationEvent(event);

        verify(applicationQueryService).upsertApplication(id, "John", "Doe", "Club", raceId, userId);
        verify(applicationQueryService, never()).deleteApplication(any());
    }

    @Test
    void handleApplicationEvent_deleted_shouldDelete() {
        UUID id = UUID.randomUUID();
        UUID raceId = UUID.randomUUID();
        ApplicationResponse payload = new ApplicationResponse(id, "Jane", "Smith", null, raceId, null);
        ApplicationEvent event = new ApplicationEvent(EventType.DELETED, payload);

        eventListener.handleApplicationEvent(event);

        verify(applicationQueryService).deleteApplication(id);
        verify(applicationQueryService, never()).upsertApplication(any(), any(), any(), any(), any(), any());
    }

    @Test
    void handleApplicationEvent_created_withNullClub_shouldUpsertWithNull() {
        UUID id = UUID.randomUUID();
        UUID raceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ApplicationResponse payload = new ApplicationResponse(id, "Bob", "Jones", null, raceId, userId);
        ApplicationEvent event = new ApplicationEvent(EventType.CREATED, payload);

        eventListener.handleApplicationEvent(event);

        verify(applicationQueryService).upsertApplication(id, "Bob", "Jones", null, raceId, userId);
    }
}

