package com.race.command.service;

import com.race.command.dto.ApplicationRequest;
import com.race.command.dto.ApplicationResponse;
import com.race.command.entity.Application;
import com.race.command.entity.User;
import com.race.command.event.ApplicationEvent;
import com.race.command.event.EventType;
import com.race.command.exception.ResourceNotFoundException;
import com.race.command.publisher.EventPublisher;
import com.race.command.repository.ApplicationRepository;
import com.race.command.repository.RaceRepository;
import com.race.command.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationCommandService {

    private final ApplicationRepository applicationRepository;
    private final RaceRepository raceRepository;
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public ApplicationResponse createApplication(ApplicationRequest request) {
        // Verify race exists
        raceRepository.findById(request.raceId())
                .orElseThrow(() -> new ResourceNotFoundException("Race", request.raceId()));

        // Get current authenticated user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElse(null);

        Application application = Application.builder()
                .id(UUID.randomUUID())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .club(request.club())
                .raceId(request.raceId())
                .userId(currentUser != null ? currentUser.getId() : null)
                .build();
        applicationRepository.save(application);
        log.info("Application created: {} by user: {}", application.getId(), email);

        ApplicationResponse response = toResponse(application);
        eventPublisher.publishApplicationEvent(new ApplicationEvent(EventType.CREATED, response));
        return response;
    }

    @Transactional
    public void deleteApplication(UUID id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application", id));
        applicationRepository.delete(application);
        log.info("Application deleted: {}", id);

        eventPublisher.publishApplicationEvent(new ApplicationEvent(EventType.DELETED, toResponse(application)));
    }

    private ApplicationResponse toResponse(Application application) {
        return new ApplicationResponse(
                application.getId(),
                application.getFirstName(),
                application.getLastName(),
                application.getClub(),
                application.getRaceId(),
                application.getUserId()
        );
    }
}

