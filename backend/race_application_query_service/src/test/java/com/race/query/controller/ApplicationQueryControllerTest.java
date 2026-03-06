package com.race.query.controller;

import com.race.query.config.SecurityConfig;
import com.race.query.dto.ApplicationResponse;
import com.race.query.security.JwtAuthFilter;
import com.race.query.security.JwtService;
import com.race.query.service.ApplicationQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

@WebMvcTest(ApplicationQueryController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtService.class})
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-that-is-at-least-256-bits-long-for-testing",
    "jwt.expiration-ms=3600000",
    "cors.allowed-origins=http://localhost:3000"
})
class ApplicationQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicationQueryService applicationQueryService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void getAllApplications_shouldReturn200WithList() throws Exception {
        UUID raceId = UUID.randomUUID();
        ApplicationResponse app = new ApplicationResponse(UUID.randomUUID(), "John", "Doe", "Trail Blazers", raceId, UUID.randomUUID());
        when(applicationQueryService.getAllApplications()).thenReturn(List.of(app));

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"))
                .andExpect(jsonPath("$[0].club").value("Trail Blazers"));
    }

    @Test
    void getAllApplications_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getApplicationById_shouldReturn200() throws Exception {
        UUID appId = UUID.randomUUID();
        UUID raceId = UUID.randomUUID();
        ApplicationResponse app = new ApplicationResponse(appId, "Jane", "Smith", null, raceId, UUID.randomUUID());
        when(applicationQueryService.getApplicationById(appId)).thenReturn(app);

        mockMvc.perform(get("/api/applications/" + appId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.club").doesNotExist());
    }

    @Test
    @WithMockUser
    void getAllApplications_empty_shouldReturn200WithEmptyList() throws Exception {
        when(applicationQueryService.getAllApplications()).thenReturn(List.of());

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}

