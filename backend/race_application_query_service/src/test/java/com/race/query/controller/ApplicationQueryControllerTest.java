package com.race.query.controller;

import com.race.query.config.SecurityConfig;
import com.race.query.dto.ApplicationResponse;
import com.race.query.enums.Role;
import com.race.query.exception.ResourceNotFoundException;
import com.race.query.security.AuthenticatedUser;
import com.race.query.security.JwtAuthFilter;
import com.race.query.security.JwtService;
import com.race.query.service.ApplicationQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationQueryController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtService.class})
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-that-is-at-least-256-bits-long-for-testing",
    "jwt.expiration-ms=3600000",
    "cors.allowed-origins=http://localhost:3000"
})
class ApplicationQueryControllerTest {

    private static final AuthenticatedUser ADMIN =
            new AuthenticatedUser(UUID.randomUUID(), "admin@trailrace.com", "Admin", "User", Role.ADMINISTRATOR, "pw");
    private static final AuthenticatedUser APPLICANT =
            new AuthenticatedUser(UUID.randomUUID(), "jane@example.com", "Jane", "Doe", Role.APPLICANT, "pw");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicationQueryService applicationQueryService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void getApplications_asAdmin_shouldReturn200WithList() throws Exception {
        UUID raceId = UUID.randomUUID();
        ApplicationResponse app = new ApplicationResponse(UUID.randomUUID(), "John", "Doe", "Trail Blazers", raceId, APPLICANT.getId());
        when(applicationQueryService.getApplicationsVisibleTo(eq(ADMIN))).thenReturn(List.of(app));

        mockMvc.perform(get("/api/applications").with(user(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"))
                .andExpect(jsonPath("$[0].club").value("Trail Blazers"));
    }

    @Test
    void getApplications_asApplicant_shouldQueryWithThatUser() throws Exception {
        ApplicationResponse own = new ApplicationResponse(UUID.randomUUID(), "Jane", "Doe", null, UUID.randomUUID(), APPLICANT.getId());
        when(applicationQueryService.getApplicationsVisibleTo(eq(APPLICANT))).thenReturn(List.of(own));

        mockMvc.perform(get("/api/applications").with(user(APPLICANT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(APPLICANT.getId().toString()));
    }

    @Test
    void getApplications_withoutAuth_shouldReturn401ProblemDetail() throws Exception {
        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void getApplicationById_shouldReturn200() throws Exception {
        UUID appId = UUID.randomUUID();
        UUID raceId = UUID.randomUUID();
        ApplicationResponse app = new ApplicationResponse(appId, "Jane", "Smith", null, raceId, APPLICANT.getId());
        when(applicationQueryService.getApplicationById(eq(appId), eq(APPLICANT))).thenReturn(app);

        mockMvc.perform(get("/api/applications/" + appId).with(user(APPLICANT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.club").doesNotExist());
    }

    @Test
    void getApplicationById_whenNotOwner_shouldReturn403ProblemDetail() throws Exception {
        UUID appId = UUID.randomUUID();
        when(applicationQueryService.getApplicationById(eq(appId), eq(APPLICANT)))
                .thenThrow(new AccessDeniedException("You can only view your own applications"));

        mockMvc.perform(get("/api/applications/" + appId).with(user(APPLICANT)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("You can only view your own applications"));
    }

    @Test
    void getApplicationById_whenNotFound_shouldReturn404ProblemDetail() throws Exception {
        UUID appId = UUID.randomUUID();
        when(applicationQueryService.getApplicationById(eq(appId), eq(ADMIN)))
                .thenThrow(new ResourceNotFoundException("Application", appId));

        mockMvc.perform(get("/api/applications/" + appId).with(user(ADMIN)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getApplicationById_withInvalidUuid_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/applications/not-a-uuid").with(user(ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getApplications_empty_shouldReturn200WithEmptyList() throws Exception {
        when(applicationQueryService.getApplicationsVisibleTo(eq(APPLICANT))).thenReturn(List.of());

        mockMvc.perform(get("/api/applications").with(user(APPLICANT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
