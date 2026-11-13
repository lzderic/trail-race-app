package com.race.query.listener;

import com.race.query.config.RabbitMQConfig;
import com.race.query.event.ApplicationEvent;
import com.race.query.event.ApplicationEventPayload;
import com.race.query.event.RaceEvent;
import com.race.query.event.RaceEventPayload;
import com.race.query.service.ApplicationQueryService;
import com.race.query.service.RaceQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Consumes integration events from the command service and applies them to the read model. */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventListener {

    private final RaceQueryService raceQueryService;
    private final ApplicationQueryService applicationQueryService;

    @RabbitListener(queues = RabbitMQConfig.RACE_QUEUE)
    public void handleRaceEvent(RaceEvent event) {
        RaceEventPayload race = event.payload();
        log.info("Received race event: type={}, id={}", event.eventType(), race.id());

        switch (event.eventType()) {
            case CREATED, UPDATED -> raceQueryService.upsertRace(race);
            case DELETED -> raceQueryService.deleteRace(race.id());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.APPLICATION_QUEUE)
    public void handleApplicationEvent(ApplicationEvent event) {
        ApplicationEventPayload application = event.payload();
        log.info("Received application event: type={}, id={}", event.eventType(), application.id());

        switch (event.eventType()) {
            case CREATED, UPDATED -> applicationQueryService.upsertApplication(application);
            case DELETED -> applicationQueryService.deleteApplication(application.id());
        }
    }
}
