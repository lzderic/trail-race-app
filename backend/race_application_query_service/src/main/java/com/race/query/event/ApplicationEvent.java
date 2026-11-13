package com.race.query.event;

public record ApplicationEvent(EventType eventType, ApplicationEventPayload payload) {}
