package com.race.command.service;

import com.race.command.dto.RacePatchRequest;
import com.race.command.dto.RaceRequest;
import com.race.command.dto.RaceResponse;
import com.race.command.entity.Race;
import com.race.command.enums.Distance;
import com.race.command.event.EventType;
import com.race.command.event.RaceEvent;
import com.race.command.exception.ResourceNotFoundException;
import com.race.command.publisher.EventPublisher;
import com.race.command.repository.ApplicationRepository;
import com.race.command.repository.RaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RaceCommandServiceTest {

    @Mock
    private RaceRepository raceRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private RaceCommandService raceCommandService;

    private Race existingRace;

    @BeforeEach
    void setUp() {
        existingRace = Race.builder()
                .id(UUID.randomUUID())
                .name("Mountain Trail")
                .distance(Distance.MARATHON)
                .build();
    }

    @Test
    void createRace_shouldSaveAndPublishEvent() {
        RaceRequest request = new RaceRequest("City Run", Distance.FIVE_K);
        when(raceRepository.save(any(Race.class))).thenAnswer(inv -> inv.getArgument(0));

        RaceResponse response = raceCommandService.createRace(request);

        assertThat(response.name()).isEqualTo("City Run");
        assertThat(response.distance()).isEqualTo(Distance.FIVE_K);
        assertThat(response.id()).isNotNull();

        ArgumentCaptor<RaceEvent> captor = ArgumentCaptor.forClass(RaceEvent.class);
        verify(eventPublisher).publishRaceEvent(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(EventType.CREATED);
    }

    @Test
    void patchRace_shouldUpdateAndPublishEvent() {
        when(raceRepository.findById(existingRace.getId())).thenReturn(Optional.of(existingRace));
        when(raceRepository.save(any(Race.class))).thenAnswer(inv -> inv.getArgument(0));

        RacePatchRequest patchRequest = new RacePatchRequest("Updated Name", Distance.TEN_K);
        RaceResponse response = raceCommandService.patchRace(existingRace.getId(), patchRequest);

        assertThat(response.name()).isEqualTo("Updated Name");
        assertThat(response.distance()).isEqualTo(Distance.TEN_K);

        ArgumentCaptor<RaceEvent> captor = ArgumentCaptor.forClass(RaceEvent.class);
        verify(eventPublisher).publishRaceEvent(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(EventType.UPDATED);
    }

    @Test
    void patchRace_whenNotFound_shouldThrowException() {
        UUID randomId = UUID.randomUUID();
        when(raceRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> raceCommandService.patchRace(randomId, new RacePatchRequest(null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteRace_shouldDeleteAndPublishEvent() {
        when(raceRepository.findById(existingRace.getId())).thenReturn(Optional.of(existingRace));
        when(applicationRepository.findByRaceId(existingRace.getId())).thenReturn(Collections.emptyList());

        raceCommandService.deleteRace(existingRace.getId());

        verify(raceRepository).delete(existingRace);
        ArgumentCaptor<RaceEvent> captor = ArgumentCaptor.forClass(RaceEvent.class);
        verify(eventPublisher).publishRaceEvent(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(EventType.DELETED);
    }

    @Test
    void deleteRace_whenNotFound_shouldThrowException() {
        UUID randomId = UUID.randomUUID();
        when(raceRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> raceCommandService.deleteRace(randomId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

