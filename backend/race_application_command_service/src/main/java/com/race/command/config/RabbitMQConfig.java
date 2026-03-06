package com.race.command.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "trail.race.exchange";
    public static final String RACE_ROUTING_KEY = "race.#";
    public static final String APPLICATION_ROUTING_KEY = "application.#";
    public static final String RACE_QUEUE = "race.queue";
    public static final String APPLICATION_QUEUE = "application.queue";

    @Bean
    public TopicExchange trailRaceExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue raceQueue() {
        return QueueBuilder.durable(RACE_QUEUE).build();
    }

    @Bean
    public Queue applicationQueue() {
        return QueueBuilder.durable(APPLICATION_QUEUE).build();
    }

    @Bean
    public Binding raceBinding(Queue raceQueue, TopicExchange trailRaceExchange) {
        return BindingBuilder.bind(raceQueue).to(trailRaceExchange).with(RACE_ROUTING_KEY);
    }

    @Bean
    public Binding applicationBinding(Queue applicationQueue, TopicExchange trailRaceExchange) {
        return BindingBuilder.bind(applicationQueue).to(trailRaceExchange).with(APPLICATION_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter(objectMapper));
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter(objectMapper));
        return template;
    }
}

