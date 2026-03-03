package com.race.query.listener;

import com.race.query.config.RabbitMQConfig;
import com.race.query.event.ApplicationEvent;
import com.race.query.event.EventType;
import com.race.query.event.RaceEvent;
import com.race.query.service.ApplicationQueryService;
import com.race.query.service.RaceQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventListener {

    private final RaceQueryService raceQueryService;
    private final ApplicationQueryService applicationQueryService;

    @RabbitListener(queues = RabbitMQConfig.RACE_QUEUE)
    public void handleRaceEvent(RaceEvent event) {
        log.info("Received race event: type={}, id={}", event.eventType(), event.payload().id());
        if (event.eventType() == EventType.DELETED) {
            raceQueryService.deleteRace(event.payload().id());
        } else {
            raceQueryService.upsertRace(
                    event.payload().id(),
                    event.payload().name(),
                    event.payload().distance()
            );
        }
    }

    @RabbitListener(queues = RabbitMQConfig.APPLICATION_QUEUE)
    public void handleApplicationEvent(ApplicationEvent event) {
        log.info("Received application event: type={}, id={}", event.eventType(), event.payload().id());
        if (event.eventType() == EventType.DELETED) {
            applicationQueryService.deleteApplication(event.payload().id());
        } else {
            applicationQueryService.upsertApplication(
                    event.payload().id(),
                    event.payload().firstName(),
                    event.payload().lastName(),
                    event.payload().club(),
                    event.payload().raceId()
            );
        }
    }
}

