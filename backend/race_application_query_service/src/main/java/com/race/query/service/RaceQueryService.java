package com.race.query.service;

import com.race.query.dto.RaceResponse;
import com.race.query.entity.Application;
import com.race.query.entity.Race;
import com.race.query.event.RaceEventPayload;
import com.race.query.exception.ResourceNotFoundException;
import com.race.query.mapper.RaceMapper;
import com.race.query.repository.ApplicationRepository;
import com.race.query.repository.RaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RaceQueryService {

    private final RaceRepository raceRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public List<RaceResponse> getAllRaces() {
        return raceRepository.findAll().stream().map(RaceMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RaceResponse getRaceById(UUID id) {
        return raceRepository.findById(id)
                .map(RaceMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Race", id));
    }

    /** Applies a CREATED / UPDATED race event to the read model. */
    @Transactional
    public void upsertRace(RaceEventPayload payload) {
        Race race = raceRepository.findById(payload.id())
                .orElseGet(() -> Race.builder().id(payload.id()).build());
        race.setName(payload.name());
        race.setDistance(payload.distance());
        raceRepository.save(race);
        log.info("Race upserted (read-side): {}", payload.id());
    }

    /** Applies a DELETED race event to the read model, removing the race's applications as well. */
    @Transactional
    public void deleteRace(UUID id) {
        raceRepository.findById(id).ifPresent(race -> {
            List<Application> applications = applicationRepository.findByRaceId(id);
            applicationRepository.deleteAll(applications);
            raceRepository.delete(race);
            log.info("Race deleted (read-side): {} (removed {} associated applications)", id, applications.size());
        });
    }
}
