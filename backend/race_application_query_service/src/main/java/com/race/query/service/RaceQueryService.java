package com.race.query.service;

import com.race.query.dto.RaceResponse;
import com.race.query.entity.Race;
import com.race.query.enums.Distance;
import com.race.query.exception.ResourceNotFoundException;
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

    @Transactional(readOnly = true)
    public List<RaceResponse> getAllRaces() {
        return raceRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RaceResponse getRaceById(UUID id) {
        return raceRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Race", id));
    }

    @Transactional
    public void upsertRace(UUID id, String name, Distance distance) {
        Race race = raceRepository.findById(id)
                .orElse(Race.builder().id(id).build());
        race.setName(name);
        race.setDistance(distance);
        raceRepository.save(race);
        log.info("Race upserted (read-side): {}", id);
    }

    @Transactional
    public void deleteRace(UUID id) {
        raceRepository.findById(id).ifPresent(race -> {
            raceRepository.delete(race);
            log.info("Race deleted (read-side): {}", id);
        });
    }

    private RaceResponse toResponse(Race race) {
        return new RaceResponse(race.getId(), race.getName(), race.getDistance());
    }
}

