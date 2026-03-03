package com.race.query.service;

import com.race.query.dto.ApplicationResponse;
import com.race.query.entity.Application;
import com.race.query.exception.ResourceNotFoundException;
import com.race.query.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationQueryServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private ApplicationQueryService applicationQueryService;

    private Application application;
    private UUID raceId;

    @BeforeEach
    void setUp() {
        raceId = UUID.randomUUID();
        application = Application.builder()
                .id(UUID.randomUUID())
                .firstName("Alice")
                .lastName("Smith")
                .club("Trail Blazers")
                .raceId(raceId)
                .build();
    }

    @Test
    void getAllApplications_shouldReturnMappedList() {
        when(applicationRepository.findAll()).thenReturn(List.of(application));

        List<ApplicationResponse> result = applicationQueryService.getAllApplications();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).firstName()).isEqualTo("Alice");
    }

    @Test
    void getApplicationById_whenExists_shouldReturn() {
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        ApplicationResponse result = applicationQueryService.getApplicationById(application.getId());

        assertThat(result.lastName()).isEqualTo("Smith");
        assertThat(result.raceId()).isEqualTo(raceId);
    }

    @Test
    void getApplicationById_whenNotFound_shouldThrow() {
        UUID randomId = UUID.randomUUID();
        when(applicationRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationQueryService.getApplicationById(randomId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void upsertApplication_newApplication_shouldSave() {
        UUID newId = UUID.randomUUID();
        when(applicationRepository.findById(newId)).thenReturn(Optional.empty());
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        applicationQueryService.upsertApplication(newId, "Bob", "Jones", null, raceId);

        ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(captor.capture());
        assertThat(captor.getValue().getFirstName()).isEqualTo("Bob");
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
}

