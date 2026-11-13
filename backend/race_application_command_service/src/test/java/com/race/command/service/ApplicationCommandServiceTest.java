package com.race.command.service;

import com.race.command.dto.ApplicationRequest;
import com.race.command.dto.ApplicationResponse;
import com.race.command.entity.Application;
import com.race.command.enums.Role;
import com.race.command.event.ApplicationEvent;
import com.race.command.event.EventType;
import com.race.command.exception.ResourceNotFoundException;
import com.race.command.publisher.EventPublisher;
import com.race.command.repository.ApplicationRepository;
import com.race.command.repository.RaceRepository;
import com.race.command.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private AuthenticatedUser applicant;
    private AuthenticatedUser otherApplicant;
    private AuthenticatedUser administrator;
    private Application existingApplication;

    @BeforeEach
    void setUp() {
        raceId = UUID.randomUUID();
        applicant = user("jane@example.com", Role.APPLICANT);
        otherApplicant = user("marco@example.com", Role.APPLICANT);
        administrator = user("admin@trailrace.com", Role.ADMINISTRATOR);

        existingApplication = Application.builder()
                .id(UUID.randomUUID())
                .firstName("Jane")
                .lastName("Doe")
                .club("Runner Club")
                .raceId(raceId)
                .userId(applicant.getId())
                .build();
    }

    @Test
    void createApplication_shouldSaveWithCurrentUserAndPublishEvent() {
        when(raceRepository.existsById(raceId)).thenReturn(true);
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        ApplicationRequest request = new ApplicationRequest("Jane", "Doe", "Runner Club", raceId);
        ApplicationResponse response = applicationCommandService.createApplication(request, applicant);

        assertThat(response.id()).isNotNull();
        assertThat(response.firstName()).isEqualTo("Jane");
        assertThat(response.raceId()).isEqualTo(raceId);
        assertThat(response.userId()).isEqualTo(applicant.getId());

        ArgumentCaptor<ApplicationEvent> captor = ArgumentCaptor.forClass(ApplicationEvent.class);
        verify(eventPublisher).publishApplicationEvent(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(EventType.CREATED);
        assertThat(captor.getValue().payload().id()).isEqualTo(response.id());
        assertThat(captor.getValue().payload().userId()).isEqualTo(applicant.getId());
    }

    @Test
    void createApplication_whenRaceNotFound_shouldThrow() {
        UUID unknownRaceId = UUID.randomUUID();
        when(raceRepository.existsById(unknownRaceId)).thenReturn(false);

        assertThatThrownBy(() -> applicationCommandService.createApplication(
                new ApplicationRequest("Jane", "Doe", null, unknownRaceId), applicant))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(applicationRepository, never()).save(any());
        verify(eventPublisher, never()).publishApplicationEvent(any());
    }

    @Test
    void deleteApplication_asOwner_shouldDeleteAndPublishEvent() {
        when(applicationRepository.findById(existingApplication.getId())).thenReturn(Optional.of(existingApplication));

        applicationCommandService.deleteApplication(existingApplication.getId(), applicant);

        verify(applicationRepository).delete(existingApplication);
        ArgumentCaptor<ApplicationEvent> captor = ArgumentCaptor.forClass(ApplicationEvent.class);
        verify(eventPublisher).publishApplicationEvent(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(EventType.DELETED);
        assertThat(captor.getValue().payload().id()).isEqualTo(existingApplication.getId());
    }

    @Test
    void deleteApplication_asAdministrator_shouldDeleteAnyApplication() {
        when(applicationRepository.findById(existingApplication.getId())).thenReturn(Optional.of(existingApplication));

        applicationCommandService.deleteApplication(existingApplication.getId(), administrator);

        verify(applicationRepository).delete(existingApplication);
    }

    @Test
    void deleteApplication_asDifferentApplicant_shouldBeDenied() {
        when(applicationRepository.findById(existingApplication.getId())).thenReturn(Optional.of(existingApplication));

        assertThatThrownBy(() -> applicationCommandService.deleteApplication(existingApplication.getId(), otherApplicant))
                .isInstanceOf(AccessDeniedException.class);

        verify(applicationRepository, never()).delete(any());
        verify(eventPublisher, never()).publishApplicationEvent(any());
    }

    @Test
    void deleteApplication_whenNotFound_shouldThrow() {
        UUID randomId = UUID.randomUUID();
        when(applicationRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationCommandService.deleteApplication(randomId, applicant))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static AuthenticatedUser user(String email, Role role) {
        return new AuthenticatedUser(UUID.randomUUID(), email, "First", "Last", role, "encoded");
    }
}
