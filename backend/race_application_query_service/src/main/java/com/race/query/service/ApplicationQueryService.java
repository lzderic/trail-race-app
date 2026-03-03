package com.race.query.service;

import com.race.query.dto.ApplicationResponse;
import com.race.query.entity.Application;
import com.race.query.exception.ResourceNotFoundException;
import com.race.query.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationQueryService {

    private final ApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getAllApplications() {
        return applicationRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(UUID id) {
        return applicationRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Application", id));
    }

    @Transactional
    public void upsertApplication(UUID id, String firstName, String lastName, String club, UUID raceId) {
        Application application = applicationRepository.findById(id)
                .orElse(Application.builder().id(id).build());
        application.setFirstName(firstName);
        application.setLastName(lastName);
        application.setClub(club);
        application.setRaceId(raceId);
        applicationRepository.save(application);
        log.info("Application upserted (read-side): {}", id);
    }

    @Transactional
    public void deleteApplication(UUID id) {
        applicationRepository.findById(id).ifPresent(app -> {
            applicationRepository.delete(app);
            log.info("Application deleted (read-side): {}", id);
        });
    }

    private ApplicationResponse toResponse(Application app) {
        return new ApplicationResponse(app.getId(), app.getFirstName(), app.getLastName(), app.getClub(), app.getRaceId());
    }
}

