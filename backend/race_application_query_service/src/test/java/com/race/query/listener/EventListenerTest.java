package com.race.query.listener;

import com.race.query.enums.Distance;
import com.race.query.event.ApplicationEvent;
import com.race.query.event.ApplicationEventPayload;
import com.race.query.event.EventType;
import com.race.query.event.RaceEvent;
import com.race.query.event.RaceEventPayload;
import com.race.query.service.ApplicationQueryService;
import com.race.query.service.RaceQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventListenerTest {

    @Mock
    private RaceQueryService raceQueryService;

    @Mock
    private ApplicationQueryService applicationQueryService;

    @InjectMocks
    private EventListener eventListener;

    @Test
    void handleRaceEvent_created_shouldUpsert() {
        RaceEventPayload payload = new RaceEventPayload(UUID.randomUUID(), "Trail 5K", Distance.FIVE_K);

        eventListener.handleRaceEvent(new RaceEvent(EventType.CREATED, payload));

        verify(raceQueryService).upsertRace(payload);
        verify(raceQueryService, never()).deleteRace(any());
    }

    @Test
    void handleRaceEvent_updated_shouldUpsert() {
        RaceEventPayload payload = new RaceEventPayload(UUID.randomUUID(), "Updated Race", Distance.TEN_K);

        eventListener.handleRaceEvent(new RaceEvent(EventType.UPDATED, payload));

        verify(raceQueryService).upsertRace(payload);
        verify(raceQueryService, never()).deleteRace(any());
    }

    @Test
    void handleRaceEvent_deleted_shouldDelete() {
        RaceEventPayload payload = new RaceEventPayload(UUID.randomUUID(), "Old Race", Distance.MARATHON);

        eventListener.handleRaceEvent(new RaceEvent(EventType.DELETED, payload));

        verify(raceQueryService).deleteRace(payload.id());
        verify(raceQueryService, never()).upsertRace(any());
    }

    @Test
    void handleApplicationEvent_created_shouldUpsert() {
        ApplicationEventPayload payload = new ApplicationEventPayload(
                UUID.randomUUID(), "John", "Doe", "Club", UUID.randomUUID(), UUID.randomUUID());

        eventListener.handleApplicationEvent(new ApplicationEvent(EventType.CREATED, payload));

        verify(applicationQueryService).upsertApplication(payload);
        verify(applicationQueryService, never()).deleteApplication(any());
    }

    @Test
    void handleApplicationEvent_deleted_shouldDelete() {
        ApplicationEventPayload payload = new ApplicationEventPayload(
                UUID.randomUUID(), "Jane", "Smith", null, UUID.randomUUID(), null);

        eventListener.handleApplicationEvent(new ApplicationEvent(EventType.DELETED, payload));

        verify(applicationQueryService).deleteApplication(payload.id());
        verify(applicationQueryService, never()).upsertApplication(any());
    }

    @Test
    void handleApplicationEvent_created_withNullClub_shouldUpsertWithNull() {
        ApplicationEventPayload payload = new ApplicationEventPayload(
                UUID.randomUUID(), "Bob", "Jones", null, UUID.randomUUID(), UUID.randomUUID());

        eventListener.handleApplicationEvent(new ApplicationEvent(EventType.CREATED, payload));

        verify(applicationQueryService).upsertApplication(payload);
    }
}
