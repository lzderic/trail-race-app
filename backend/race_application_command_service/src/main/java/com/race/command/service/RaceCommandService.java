package com.race.command.service;

import com.race.command.dto.RacePatchRequest;
import com.race.command.dto.RaceRequest;
import com.race.command.dto.RaceResponse;
import com.race.command.entity.Race;
import com.race.command.event.EventType;
import com.race.command.event.RaceEvent;
import com.race.command.exception.ResourceNotFoundException;
import com.race.command.publisher.EventPublisher;
import com.race.command.repository.RaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RaceCommandService {

    private final RaceRepository raceRepository;
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

        RaceResponse response = toResponse(race);
        eventPublisher.publishRaceEvent(new RaceEvent(EventType.CREATED, response));
        return response;
    }

    @Transactional
    public RaceResponse patchRace(UUID id, RacePatchRequest request) {
        Race race = raceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Race", id));

        if (request.name() != null) {
            race.setName(request.name());
        }
        if (request.distance() != null) {
            race.setDistance(request.distance());
        }
        raceRepository.save(race);
        log.info("Race updated: {}", race.getId());

        RaceResponse response = toResponse(race);
        eventPublisher.publishRaceEvent(new RaceEvent(EventType.UPDATED, response));
        return response;
    }

    @Transactional
    public void deleteRace(UUID id) {
        Race race = raceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Race", id));
        raceRepository.delete(race);
        log.info("Race deleted: {}", id);

        eventPublisher.publishRaceEvent(new RaceEvent(EventType.DELETED, toResponse(race)));
    }

    private RaceResponse toResponse(Race race) {
        return new RaceResponse(race.getId(), race.getName(), race.getDistance());
    }
}

