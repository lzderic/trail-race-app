package com.race.command.publisher;
import com.race.command.config.RabbitMQConfig;
import com.race.command.dto.RaceResponse;
import com.race.command.dto.ApplicationResponse;
import com.race.command.enums.Distance;
import com.race.command.event.ApplicationEvent;
import com.race.command.event.EventType;
import com.race.command.event.RaceEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
@ExtendWith(MockitoExtension.class)
class EventPublisherTest {
    @Mock private RabbitTemplate rabbitTemplate;
    @InjectMocks private EventPublisher eventPublisher;
    @Test
    void publishRaceEvent_created_shouldSendWithCorrectRoutingKey() {
        RaceResponse payload = new RaceResponse(UUID.randomUUID(), "Test Race", Distance.FIVE_K);
        RaceEvent event = new RaceEvent(EventType.CREATED, payload);
        eventPublisher.publishRaceEvent(event);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq("race.created"), eq(event));
    }
    @Test
    void publishRaceEvent_deleted_shouldSendWithCorrectRoutingKey() {
        RaceResponse payload = new RaceResponse(UUID.randomUUID(), "Old Race", Distance.MARATHON);
        RaceEvent event = new RaceEvent(EventType.DELETED, payload);
        eventPublisher.publishRaceEvent(event);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq("race.deleted"), eq(event));
    }
    @Test
    void publishRaceEvent_updated_shouldSendWithCorrectRoutingKey() {
        RaceResponse payload = new RaceResponse(UUID.randomUUID(), "Updated Race", Distance.TEN_K);
        RaceEvent event = new RaceEvent(EventType.UPDATED, payload);
        eventPublisher.publishRaceEvent(event);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq("race.updated"), eq(event));
    }
    @Test
    void publishApplicationEvent_created_shouldSendWithCorrectRoutingKey() {
        ApplicationResponse payload = new ApplicationResponse(UUID.randomUUID(), "John", "Doe", "Club", UUID.randomUUID(), UUID.randomUUID());
        ApplicationEvent event = new ApplicationEvent(EventType.CREATED, payload);
        eventPublisher.publishApplicationEvent(event);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq("application.created"), eq(event));
    }
    @Test
    void publishApplicationEvent_deleted_shouldSendWithCorrectRoutingKey() {
        ApplicationResponse payload = new ApplicationResponse(UUID.randomUUID(), "Jane", "Smith", null, UUID.randomUUID(), null);
        ApplicationEvent event = new ApplicationEvent(EventType.DELETED, payload);
        eventPublisher.publishApplicationEvent(event);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq("application.deleted"), eq(event));
    }
}