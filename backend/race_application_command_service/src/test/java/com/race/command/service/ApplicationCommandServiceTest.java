package com.race.command.service;

import com.race.command.dto.ApplicationRequest;
import com.race.command.dto.ApplicationResponse;
import com.race.command.entity.Application;
import com.race.command.entity.Race;
import com.race.command.enums.Distance;
import com.race.command.event.ApplicationEvent;
import com.race.command.event.EventType;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationCommandServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private RaceRepository raceRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ApplicationCommandService applicationCommandService;

    private UUID raceId;
    private Race existingRace;
    private Application existingApplication;

    @BeforeEach
    void setUp() {
        raceId = UUID.randomUUID();
        existingRace = Race.builder()
                .id(raceId)
                .name("Forest Run")
                .distance(Distance.HALF_MARATHON)
                .build();

        existingApplication = Application.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .club("Runner Club")
                .raceId(raceId)
                .build();
    }

    @Test
    void createApplication_shouldSaveAndPublishEvent() {
        when(raceRepository.findById(raceId)).thenReturn(Optional.of(existingRace));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        ApplicationRequest request = new ApplicationRequest("John", "Doe", "Runner Club", raceId);
        ApplicationResponse response = applicationCommandService.createApplication(request);

        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.raceId()).isEqualTo(raceId);

        ArgumentCaptor<ApplicationEvent> captor = ArgumentCaptor.forClass(ApplicationEvent.class);
        verify(eventPublisher).publishApplicationEvent(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(EventType.CREATED);
    }

    @Test
    void createApplication_whenRaceNotFound_shouldThrow() {
        UUID unknownRaceId = UUID.randomUUID();
        when(raceRepository.findById(unknownRaceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationCommandService.createApplication(
                new ApplicationRequest("John", "Doe", null, unknownRaceId)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteApplication_shouldDeleteAndPublishEvent() {
        when(applicationRepository.findById(existingApplication.getId())).thenReturn(Optional.of(existingApplication));

        applicationCommandService.deleteApplication(existingApplication.getId());

        verify(applicationRepository).delete(existingApplication);
        ArgumentCaptor<ApplicationEvent> captor = ArgumentCaptor.forClass(ApplicationEvent.class);
        verify(eventPublisher).publishApplicationEvent(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(EventType.DELETED);
    }

    @Test
    void deleteApplication_whenNotFound_shouldThrow() {
        UUID randomId = UUID.randomUUID();
        when(applicationRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationCommandService.deleteApplication(randomId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

