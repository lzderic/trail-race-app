package com.race.command.event;

import com.race.command.dto.ApplicationResponse;

public record ApplicationEvent(EventType eventType, ApplicationResponse payload) {}

