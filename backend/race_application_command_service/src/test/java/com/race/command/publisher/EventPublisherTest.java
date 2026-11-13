package com.race.command.publisher;

import com.race.command.config.RabbitMQConfig;
import com.race.command.enums.Distance;
import com.race.command.event.ApplicationEvent;
import com.race.command.event.ApplicationEventPayload;
import com.race.command.event.EventType;
import com.race.command.event.RaceEvent;
import com.race.command.event.RaceEventPayload;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EventPublisher eventPublisher;

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void publishRaceEvent_created_shouldSendWithCorrectRoutingKey() {
        RaceEvent event = new RaceEvent(EventType.CREATED, racePayload("Test Race", Distance.FIVE_K));

        eventPublisher.publishRaceEvent(event);

        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq("race.created"), eq(event));
    }

    @Test
    void publishRaceEvent_deleted_shouldSendWithCorrectRoutingKey() {
        RaceEvent event = new RaceEvent(EventType.DELETED, racePayload("Old Race", Distance.MARATHON));

        eventPublisher.publishRaceEvent(event);

        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq("race.deleted"), eq(event));
    }

    @Test
    void publishRaceEvent_updated_shouldSendWithCorrectRoutingKey() {
        RaceEvent event = new RaceEvent(EventType.UPDATED, racePayload("Updated Race", Distance.TEN_K));

        eventPublisher.publishRaceEvent(event);

        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq("race.updated"), eq(event));
    }

    @Test
    void publishApplicationEvent_created_shouldSendWithCorrectRoutingKey() {
        ApplicationEvent event = new ApplicationEvent(EventType.CREATED, applicationPayload("Club", UUID.randomUUID()));

        eventPublisher.publishApplicationEvent(event);

        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq("application.created"), eq(event));
    }

    @Test
    void publishApplicationEvent_deleted_shouldSendWithCorrectRoutingKey() {
        ApplicationEvent event = new ApplicationEvent(EventType.DELETED, applicationPayload(null, null));

        eventPublisher.publishApplicationEvent(event);

        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq("application.deleted"), eq(event));
    }

    @Test
    void publish_insideTransaction_shouldDeferSendingUntilAfterCommit() {
        TransactionSynchronizationManager.initSynchronization();
        RaceEvent event = new RaceEvent(EventType.CREATED, racePayload("Deferred", Distance.FIVE_K));

        eventPublisher.publishRaceEvent(event);
        verify(rabbitTemplate, never()).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq("race.created"), eq(event));

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq("race.created"), eq(event));
    }

    private static RaceEventPayload racePayload(String name, Distance distance) {
        return new RaceEventPayload(UUID.randomUUID(), name, distance);
    }

    private static ApplicationEventPayload applicationPayload(String club, UUID userId) {
        return new ApplicationEventPayload(UUID.randomUUID(), "John", "Doe", club, UUID.randomUUID(), userId);
    }
}
