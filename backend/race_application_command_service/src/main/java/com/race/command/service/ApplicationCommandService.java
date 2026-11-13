package com.race.command.service;

import com.race.command.dto.ApplicationRequest;
import com.race.command.dto.ApplicationResponse;
import com.race.command.entity.Application;
import com.race.command.event.ApplicationEvent;
import com.race.command.event.EventType;
import com.race.command.exception.ResourceNotFoundException;
import com.race.command.mapper.ApplicationMapper;
import com.race.command.publisher.EventPublisher;
import com.race.command.repository.ApplicationRepository;
import com.race.command.repository.RaceRepository;
import com.race.command.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationCommandService {

    private final ApplicationRepository applicationRepository;
    private final RaceRepository raceRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public ApplicationResponse createApplication(ApplicationRequest request, AuthenticatedUser currentUser) {
        if (!raceRepository.existsById(request.raceId())) {
            throw new ResourceNotFoundException("Race", request.raceId());
        }

        Application application = Application.builder()
                .id(UUID.randomUUID())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .club(request.club())
                .raceId(request.raceId())
                .userId(currentUser.getId())
                .build();
        applicationRepository.save(application);
        log.info("Application created: {} by user: {}", application.getId(), currentUser.getEmail());

        eventPublisher.publishApplicationEvent(
                new ApplicationEvent(EventType.CREATED, ApplicationMapper.toEventPayload(application)));
        return ApplicationMapper.toResponse(application);
    }

    /**
     * Deletes an application. Administrators may delete any application;
     * applicants may only delete the applications they submitted themselves.
     */
    @Transactional
    public void deleteApplication(UUID id, AuthenticatedUser currentUser) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application", id));

        if (!canManage(application, currentUser)) {
            throw new AccessDeniedException("You can only delete your own applications");
        }

        applicationRepository.delete(application);
        log.info("Application deleted: {} by user: {}", id, currentUser.getEmail());

        eventPublisher.publishApplicationEvent(
                new ApplicationEvent(EventType.DELETED, ApplicationMapper.toEventPayload(application)));
    }

    private static boolean canManage(Application application, AuthenticatedUser user) {
        return user.isAdministrator() || user.getId().equals(application.getUserId());
    }
}
