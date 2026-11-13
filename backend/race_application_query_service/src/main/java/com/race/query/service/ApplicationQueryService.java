package com.race.query.service;

import com.race.query.dto.ApplicationResponse;
import com.race.query.entity.Application;
import com.race.query.event.ApplicationEventPayload;
import com.race.query.exception.ResourceNotFoundException;
import com.race.query.mapper.ApplicationMapper;
import com.race.query.repository.ApplicationRepository;
import com.race.query.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationQueryService {

    private final ApplicationRepository applicationRepository;

    /**
     * Returns the applications the given user is allowed to see:
     * administrators see every application, applicants only their own.
     */
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsVisibleTo(AuthenticatedUser user) {
        List<Application> applications = user.isAdministrator()
                ? applicationRepository.findAll()
                : applicationRepository.findByUserId(user.getId());
        return applications.stream().map(ApplicationMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(UUID id, AuthenticatedUser user) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application", id));

        if (!canView(application, user)) {
            throw new AccessDeniedException("You can only view your own applications");
        }
        return ApplicationMapper.toResponse(application);
    }

    /** Applies a CREATED / UPDATED application event to the read model. */
    @Transactional
    public void upsertApplication(ApplicationEventPayload payload) {
        Application application = applicationRepository.findById(payload.id())
                .orElseGet(() -> Application.builder().id(payload.id()).build());
        application.setFirstName(payload.firstName());
        application.setLastName(payload.lastName());
        application.setClub(payload.club());
        application.setRaceId(payload.raceId());
        application.setUserId(payload.userId());
        applicationRepository.save(application);
        log.info("Application upserted (read-side): {}", payload.id());
    }

    /** Applies a DELETED application event to the read model. */
    @Transactional
    public void deleteApplication(UUID id) {
        applicationRepository.findById(id).ifPresent(application -> {
            applicationRepository.delete(application);
            log.info("Application deleted (read-side): {}", id);
        });
    }

    private static boolean canView(Application application, AuthenticatedUser user) {
        return user.isAdministrator() || user.getId().equals(application.getUserId());
    }
}
