package com.race.query.service;

import com.race.query.dto.ApplicationResponse;
import com.race.query.entity.Application;
import com.race.query.enums.Role;
import com.race.query.event.ApplicationEventPayload;
import com.race.query.exception.ResourceNotFoundException;
import com.race.query.repository.ApplicationRepository;
import com.race.query.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationQueryServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private ApplicationQueryService applicationQueryService;

    private UUID raceId;
    private AuthenticatedUser owner;
    private AuthenticatedUser otherApplicant;
    private AuthenticatedUser administrator;
    private Application application;

    @BeforeEach
    void setUp() {
        raceId = UUID.randomUUID();
        owner = user("alice@example.com", Role.APPLICANT);
        otherApplicant = user("bob@example.com", Role.APPLICANT);
        administrator = user("admin@trailrace.com", Role.ADMINISTRATOR);

        application = Application.builder()
                .id(UUID.randomUUID())
                .firstName("Alice")
                .lastName("Smith")
                .club("Trail Blazers")
                .raceId(raceId)
                .userId(owner.getId())
                .build();
    }

    @Test
    void getApplicationsVisibleTo_asAdministrator_shouldReturnAll() {
        when(applicationRepository.findAll()).thenReturn(List.of(application));

        List<ApplicationResponse> result = applicationQueryService.getApplicationsVisibleTo(administrator);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).firstName()).isEqualTo("Alice");
        verify(applicationRepository, never()).findByUserId(any());
    }

    @Test
    void getApplicationsVisibleTo_asApplicant_shouldReturnOnlyOwn() {
        when(applicationRepository.findByUserId(owner.getId())).thenReturn(List.of(application));

        List<ApplicationResponse> result = applicationQueryService.getApplicationsVisibleTo(owner);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).userId()).isEqualTo(owner.getId());
        verify(applicationRepository, never()).findAll();
    }

    @Test
    void getApplicationById_asOwner_shouldReturn() {
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        ApplicationResponse result = applicationQueryService.getApplicationById(application.getId(), owner);

        assertThat(result.lastName()).isEqualTo("Smith");
        assertThat(result.raceId()).isEqualTo(raceId);
    }

    @Test
    void getApplicationById_asAdministrator_shouldReturnAnyApplication() {
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        ApplicationResponse result = applicationQueryService.getApplicationById(application.getId(), administrator);

        assertThat(result.id()).isEqualTo(application.getId());
    }

    @Test
    void getApplicationById_asDifferentApplicant_shouldBeDenied() {
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationQueryService.getApplicationById(application.getId(), otherApplicant))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getApplicationById_whenNotFound_shouldThrow() {
        UUID randomId = UUID.randomUUID();
        when(applicationRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationQueryService.getApplicationById(randomId, owner))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void upsertApplication_newApplication_shouldSaveAllFields() {
        ApplicationEventPayload payload = new ApplicationEventPayload(
                UUID.randomUUID(), "Bob", "Jones", null, raceId, otherApplicant.getId());
        when(applicationRepository.findById(payload.id())).thenReturn(Optional.empty());
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        applicationQueryService.upsertApplication(payload);

        ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(captor.capture());
        Application saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(payload.id());
        assertThat(saved.getFirstName()).isEqualTo("Bob");
        assertThat(saved.getLastName()).isEqualTo("Jones");
        assertThat(saved.getClub()).isNull();
        assertThat(saved.getRaceId()).isEqualTo(raceId);
        assertThat(saved.getUserId()).isEqualTo(otherApplicant.getId());
    }

    @Test
    void upsertApplication_existingApplication_shouldUpdateInPlace() {
        ApplicationEventPayload payload = new ApplicationEventPayload(
                application.getId(), "Alicia", "Smith", "New Club", raceId, owner.getId());
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        applicationQueryService.upsertApplication(payload);

        verify(applicationRepository).save(application);
        assertThat(application.getFirstName()).isEqualTo("Alicia");
        assertThat(application.getClub()).isEqualTo("New Club");
    }

    @Test
    void deleteApplication_whenExists_shouldDelete() {
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        applicationQueryService.deleteApplication(application.getId());

        verify(applicationRepository).delete(application);
    }

    @Test
    void deleteApplication_whenNotExists_shouldDoNothing() {
        UUID randomId = UUID.randomUUID();
        when(applicationRepository.findById(randomId)).thenReturn(Optional.empty());

        applicationQueryService.deleteApplication(randomId);

        verify(applicationRepository, never()).delete(any());
    }

    private static AuthenticatedUser user(String email, Role role) {
        return new AuthenticatedUser(UUID.randomUUID(), email, "First", "Last", role, "encoded");
    }
}
