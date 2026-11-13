package com.race.command.publisher;

import com.race.command.config.RabbitMQConfig;
import com.race.command.event.ApplicationEvent;
import com.race.command.event.EventType;
import com.race.command.event.RaceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Locale;

/**
 * Publishes integration events to RabbitMQ.
 * <p>
 * When called inside a transaction the event is sent only after that transaction commits,
 * so the read side never receives an event for a write that was rolled back.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private static final String RACE_ROUTING_PREFIX = "race.";
    private static final String APPLICATION_ROUTING_PREFIX = "application.";

    private final RabbitTemplate rabbitTemplate;

    public void publishRaceEvent(RaceEvent event) {
        publish(RACE_ROUTING_PREFIX + routingSuffix(event.eventType()), event);
    }

    public void publishApplicationEvent(ApplicationEvent event) {
        publish(APPLICATION_ROUTING_PREFIX + routingSuffix(event.eventType()), event);
    }

    private void publish(String routingKey, Object event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(routingKey, event);
                }
            });
        } else {
            send(routingKey, event);
        }
    }

    private void send(String routingKey, Object event) {
        log.info("Publishing event with routing key: {}", routingKey);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, event);
    }

    private static String routingSuffix(EventType eventType) {
        return eventType.name().toLowerCase(Locale.ROOT);
    }
}
