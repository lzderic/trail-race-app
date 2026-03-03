package com.race.query.controller;

import com.race.query.config.SecurityConfig;
import com.race.query.dto.RaceResponse;
import com.race.query.enums.Distance;
import com.race.query.security.JwtAuthFilter;
import com.race.query.security.JwtService;
import com.race.query.service.RaceQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RaceQueryController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtService.class})
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-that-is-at-least-256-bits-long-for-testing",
    "jwt.expiration-ms=3600000"
})
class RaceQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RaceQueryService raceQueryService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void getAllRaces_shouldReturn200WithList() throws Exception {
        RaceResponse race = new RaceResponse(UUID.randomUUID(), "Forest 5K", Distance.FIVE_K);
        when(raceQueryService.getAllRaces()).thenReturn(List.of(race));

        mockMvc.perform(get("/api/races"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Forest 5K"))
                .andExpect(jsonPath("$[0].distance").value("FIVE_K"));
    }

    @Test
    void getAllRaces_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/races"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getRaceById_shouldReturn200() throws Exception {
        UUID raceId = UUID.randomUUID();
        RaceResponse race = new RaceResponse(raceId, "Marathon Classic", Distance.MARATHON);
        when(raceQueryService.getRaceById(raceId)).thenReturn(race);

        mockMvc.perform(get("/api/races/" + raceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Marathon Classic"));
    }
}




