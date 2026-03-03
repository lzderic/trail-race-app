package com.race.command.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.race.command.config.SecurityConfig;
import com.race.command.dto.RacePatchRequest;
import com.race.command.dto.RaceRequest;
import com.race.command.dto.RaceResponse;
import com.race.command.enums.Distance;
import com.race.command.security.JwtAuthFilter;
import com.race.command.security.JwtService;
import com.race.command.service.RaceCommandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RaceCommandController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtService.class})
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-that-is-at-least-256-bits-long-for-testing",
    "jwt.expiration-ms=3600000"
})
class RaceCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RaceCommandService raceCommandService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void createRace_shouldReturn201() throws Exception {
        RaceRequest request = new RaceRequest("Mountain Trail", Distance.MARATHON);
        RaceResponse response = new RaceResponse(UUID.randomUUID(), "Mountain Trail", Distance.MARATHON);
        when(raceCommandService.createRace(any())).thenReturn(response);

        mockMvc.perform(post("/api/races")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Mountain Trail"))
                .andExpect(jsonPath("$.distance").value("MARATHON"));
    }

    @Test
    @WithMockUser(roles = "APPLICANT")
    void createRace_asApplicant_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/races")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RaceRequest("Test", Distance.FIVE_K))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void patchRace_shouldReturn200() throws Exception {
        UUID raceId = UUID.randomUUID();
        RacePatchRequest patchRequest = new RacePatchRequest("Updated", null);
        RaceResponse response = new RaceResponse(raceId, "Updated", Distance.MARATHON);
        when(raceCommandService.patchRace(eq(raceId), any())).thenReturn(response);

        mockMvc.perform(patch("/api/races/" + raceId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void deleteRace_shouldReturn204() throws Exception {
        UUID raceId = UUID.randomUUID();

        mockMvc.perform(delete("/api/races/" + raceId).with(csrf()))
                .andExpect(status().isNoContent());

        verify(raceCommandService).deleteRace(raceId);
    }
}


