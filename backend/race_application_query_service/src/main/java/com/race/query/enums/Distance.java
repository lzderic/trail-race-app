package com.race.query.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Race distance. Serialised to JSON using the short label (for example {@code "HalfMarathon"});
 * parsing accepts either that label or the enum constant name, case-insensitively.
 */
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
        return Arrays.stream(values())
                .filter(distance -> distance.value.equalsIgnoreCase(value) || distance.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown distance: " + value + ". Allowed values: " + allowedValues()));
    }

    private static String allowedValues() {
        return Arrays.stream(values()).map(Distance::getValue).collect(Collectors.joining(", "));
    }
}
