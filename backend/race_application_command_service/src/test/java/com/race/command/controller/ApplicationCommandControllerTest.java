package com.race.command.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.race.command.config.SecurityConfig;
import com.race.command.dto.ApplicationRequest;
import com.race.command.dto.ApplicationResponse;
import com.race.command.security.JwtAuthFilter;
import com.race.command.security.JwtService;
import com.race.command.service.ApplicationCommandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplicationCommandController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtService.class})
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-that-is-at-least-256-bits-long-for-testing",
    "jwt.expiration-ms=3600000",
    "cors.allowed-origins=http://localhost:3000"
})
class ApplicationCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApplicationCommandService applicationCommandService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void createApplication_asAdmin_shouldReturn201() throws Exception {
        UUID raceId = UUID.randomUUID();
        ApplicationRequest request = new ApplicationRequest("John", "Doe", "Trail Blazers", raceId);
        ApplicationResponse response = new ApplicationResponse(UUID.randomUUID(), "John", "Doe", "Trail Blazers", raceId, UUID.randomUUID());
        when(applicationCommandService.createApplication(any())).thenReturn(response);

        mockMvc.perform(post("/api/applications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.club").value("Trail Blazers"));
    }

    @Test
    @WithMockUser(roles = "APPLICANT")
    void createApplication_asApplicant_shouldReturn201() throws Exception {
        UUID raceId = UUID.randomUUID();
        ApplicationRequest request = new ApplicationRequest("Jane", "Smith", null, raceId);
        ApplicationResponse response = new ApplicationResponse(UUID.randomUUID(), "Jane", "Smith", null, raceId, UUID.randomUUID());
        when(applicationCommandService.createApplication(any())).thenReturn(response);

        mockMvc.perform(post("/api/applications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    void createApplication_withoutAuth_shouldReturn401() throws Exception {
        UUID raceId = UUID.randomUUID();
        ApplicationRequest request = new ApplicationRequest("John", "Doe", null, raceId);

        mockMvc.perform(post("/api/applications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void createApplication_withMissingFirstName_shouldReturn400() throws Exception {
        UUID raceId = UUID.randomUUID();
        ApplicationRequest request = new ApplicationRequest("", "Doe", null, raceId);

        mockMvc.perform(post("/api/applications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void deleteApplication_asAdmin_shouldReturn204() throws Exception {
        UUID appId = UUID.randomUUID();

        mockMvc.perform(delete("/api/applications/" + appId).with(csrf()))
                .andExpect(status().isNoContent());

        verify(applicationCommandService).deleteApplication(appId);
    }

    @Test
    @WithMockUser(roles = "APPLICANT")
    void deleteApplication_asApplicant_shouldReturn204() throws Exception {
        UUID appId = UUID.randomUUID();

        mockMvc.perform(delete("/api/applications/" + appId).with(csrf()))
                .andExpect(status().isNoContent());

        verify(applicationCommandService).deleteApplication(appId);
    }
}

