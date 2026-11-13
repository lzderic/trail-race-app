package com.race.command.service;

import com.race.command.dto.RacePatchRequest;
import com.race.command.dto.RaceRequest;
import com.race.command.dto.RaceResponse;
import com.race.command.entity.Application;
import com.race.command.entity.Race;
import com.race.command.event.ApplicationEvent;
import com.race.command.event.EventType;
import com.race.command.event.RaceEvent;
import com.race.command.exception.ResourceNotFoundException;
import com.race.command.mapper.ApplicationMapper;
import com.race.command.mapper.RaceMapper;
import com.race.command.publisher.EventPublisher;
import com.race.command.repository.ApplicationRepository;
import com.race.command.repository.RaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RaceCommandService {

    private final RaceRepository raceRepository;
    private final ApplicationRepository applicationRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public RaceResponse createRace(RaceRequest request) {
        Race race = Race.builder()
                .id(UUID.randomUUID())
                .name(request.name())
                .distance(request.distance())
                .build();
        raceRepository.save(race);
        log.info("Race created: {}", race.getId());

        eventPublisher.publishRaceEvent(new RaceEvent(EventType.CREATED, RaceMapper.toEventPayload(race)));
        return RaceMapper.toResponse(race);
    }

    @Transactional
    public RaceResponse patchRace(UUID id, RacePatchRequest request) {
        Race race = findRace(id);

        if (request.name() != null) {
            race.setName(request.name());
        }
        if (request.distance() != null) {
            race.setDistance(request.distance());
        }
        raceRepository.save(race);
        log.info("Race updated: {}", race.getId());

        eventPublisher.publishRaceEvent(new RaceEvent(EventType.UPDATED, RaceMapper.toEventPayload(race)));
        return RaceMapper.toResponse(race);
    }

    /**
     * Deletes a race together with every application submitted for it, publishing one
     * DELETED event per removed application followed by the race's own DELETED event.
     */
    @Transactional
    public void deleteRace(UUID id) {
        Race race = findRace(id);
        List<Application> applications = applicationRepository.findByRaceId(id);

        applicationRepository.deleteAll(applications);
        raceRepository.delete(race);
        log.info("Race deleted: {} (removed {} associated applications)", id, applications.size());

        applications.forEach(application -> eventPublisher.publishApplicationEvent(
                new ApplicationEvent(EventType.DELETED, ApplicationMapper.toEventPayload(application))));
        eventPublisher.publishRaceEvent(new RaceEvent(EventType.DELETED, RaceMapper.toEventPayload(race)));
    }

    private Race findRace(UUID id) {
        return raceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Race", id));
    }
}
