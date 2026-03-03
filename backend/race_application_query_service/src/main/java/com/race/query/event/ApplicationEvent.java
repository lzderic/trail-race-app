package com.race.query.event;

import com.race.query.dto.ApplicationResponse;

public record ApplicationEvent(EventType eventType, ApplicationResponse payload) {}

