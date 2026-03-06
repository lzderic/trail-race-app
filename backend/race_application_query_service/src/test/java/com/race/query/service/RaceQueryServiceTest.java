package com.race.query.service;

import com.race.query.dto.RaceResponse;
import com.race.query.entity.Race;
import com.race.query.enums.Distance;
import com.race.query.exception.ResourceNotFoundException;
import com.race.query.repository.ApplicationRepository;
import com.race.query.repository.RaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RaceQueryServiceTest {

    @Mock
    private RaceRepository raceRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private RaceQueryService raceQueryService;

    private Race race;

    @BeforeEach
    void setUp() {
        race = Race.builder()
                .id(UUID.randomUUID())
                .name("Coastal Trail")
                .distance(Distance.HALF_MARATHON)
                .build();
    }

    @Test
    void getAllRaces_shouldReturnMappedList() {
        when(raceRepository.findAll()).thenReturn(List.of(race));

        List<RaceResponse> result = raceQueryService.getAllRaces();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Coastal Trail");
    }

    @Test
    void getRaceById_whenExists_shouldReturnResponse() {
        when(raceRepository.findById(race.getId())).thenReturn(Optional.of(race));

        RaceResponse result = raceQueryService.getRaceById(race.getId());

        assertThat(result.id()).isEqualTo(race.getId());
        assertThat(result.distance()).isEqualTo(Distance.HALF_MARATHON);
    }

    @Test
    void getRaceById_whenNotFound_shouldThrow() {
        UUID randomId = UUID.randomUUID();
        when(raceRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> raceQueryService.getRaceById(randomId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void upsertRace_newRace_shouldSave() {
        UUID newId = UUID.randomUUID();
        when(raceRepository.findById(newId)).thenReturn(Optional.empty());
        when(raceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        raceQueryService.upsertRace(newId, "New Race", Distance.MARATHON);

        ArgumentCaptor<Race> captor = ArgumentCaptor.forClass(Race.class);
        verify(raceRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("New Race");
    }

    @Test
    void deleteRace_whenExists_shouldDelete() {
        when(raceRepository.findById(race.getId())).thenReturn(Optional.of(race));
        when(applicationRepository.findByRaceId(race.getId())).thenReturn(Collections.emptyList());

        raceQueryService.deleteRace(race.getId());

        verify(raceRepository).delete(race);
    }

    @Test
    void deleteRace_whenNotExists_shouldDoNothing() {
        UUID randomId = UUID.randomUUID();
        when(raceRepository.findById(randomId)).thenReturn(Optional.empty());

        raceQueryService.deleteRace(randomId);

        verify(raceRepository, never()).delete(any());
    }
}

