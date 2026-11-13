package com.race.command.event;

public record ApplicationEvent(EventType eventType, ApplicationEventPayload payload) {}
