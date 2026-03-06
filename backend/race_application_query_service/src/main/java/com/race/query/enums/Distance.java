package com.race.query.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Distance {
    FIVE_K("5k"),
    TEN_K("10k"),
    HALF_MARATHON("HalfMarathon"),
    MARATHON("Marathon");

    private final String value;

    Distance(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Distance fromValue(String value) {
        for (Distance d : values()) {
            if (d.value.equalsIgnoreCase(value)) {
                return d;
            }
        }
        throw new IllegalArgumentException("Unknown distance: " + value);
    }
}
