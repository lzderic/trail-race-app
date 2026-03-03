package com.race.command.publisher;

import com.race.command.config.RabbitMQConfig;
import com.race.command.event.ApplicationEvent;
import com.race.command.event.RaceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishRaceEvent(RaceEvent event) {
        String routingKey = "race." + event.eventType().name().toLowerCase();
        log.info("Publishing race event: {} with routing key: {}", event.eventType(), routingKey);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, event);
    }

    public void publishApplicationEvent(ApplicationEvent event) {
        String routingKey = "application." + event.eventType().name().toLowerCase();
        log.info("Publishing application event: {} with routing key: {}", event.eventType(), routingKey);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, event);
    }
}

