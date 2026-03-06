package com.race.command.enums;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DistanceEnumTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ParameterizedTest
    @CsvSource({"FIVE_K,5k", "TEN_K,10k", "HALF_MARATHON,HalfMarathon", "MARATHON,Marathon"})
    void getValue_shouldReturnJsonValue(String enumName, String expectedValue) {
        Distance distance = Distance.valueOf(enumName);
        assertThat(distance.getValue()).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @CsvSource({"5k,FIVE_K", "10k,TEN_K", "HalfMarathon,HALF_MARATHON", "Marathon,MARATHON"})
    void fromValue_shouldParseCorrectly(String value, String expectedEnum) {
        assertThat(Distance.fromValue(value)).isEqualTo(Distance.valueOf(expectedEnum));
    }

    @Test
    void fromValue_caseInsensitive_shouldWork() {
        assertThat(Distance.fromValue("5K")).isEqualTo(Distance.FIVE_K);
        assertThat(Distance.fromValue("marathon")).isEqualTo(Distance.MARATHON);
    }

    @Test
    void fromValue_withUnknown_shouldThrow() {
        assertThatThrownBy(() -> Distance.fromValue("100k"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown distance");
    }

    @Test
    void jsonSerialization_shouldUseJsonValue() throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(Distance.FIVE_K);
        assertThat(json).isEqualTo("\"5k\"");
    }

    @Test
    void jsonDeserialization_shouldUseJsonCreator() throws JsonProcessingException {
        Distance distance = objectMapper.readValue("\"HalfMarathon\"", Distance.class);
        assertThat(distance).isEqualTo(Distance.HALF_MARATHON);
    }
}