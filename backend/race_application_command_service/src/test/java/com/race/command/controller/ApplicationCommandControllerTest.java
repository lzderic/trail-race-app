package com.race.command.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.race.command.config.SecurityConfig;
import com.race.command.dto.ApplicationRequest;
import com.race.command.dto.ApplicationResponse;
import com.race.command.enums.Role;
import com.race.command.security.AuthenticatedUser;
import com.race.command.security.JwtAuthFilter;
import com.race.command.security.JwtService;
import com.race.command.service.ApplicationCommandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationCommandController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtService.class})
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-that-is-at-least-256-bits-long-for-testing",
    "jwt.expiration-ms=3600000",
    "cors.allowed-origins=http://localhost:3000"
})
class ApplicationCommandControllerTest {

    private static final AuthenticatedUser ADMIN =
            new AuthenticatedUser(UUID.randomUUID(), "admin@trailrace.com", "Admin", "User", Role.ADMINISTRATOR, "pw");
    private static final AuthenticatedUser APPLICANT =
            new AuthenticatedUser(UUID.randomUUID(), "jane@example.com", "Jane", "Doe", Role.APPLICANT, "pw");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApplicationCommandService applicationCommandService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void createApplication_asAdmin_shouldReturn201() throws Exception {
        UUID raceId = UUID.randomUUID();
        ApplicationRequest request = new ApplicationRequest("John", "Doe", "Trail Blazers", raceId);
        ApplicationResponse response = new ApplicationResponse(UUID.randomUUID(), "John", "Doe", "Trail Blazers", raceId, ADMIN.getId());
        when(applicationCommandService.createApplication(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/applications")
                        .with(user(ADMIN))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.club").value("Trail Blazers"))
                .andExpect(jsonPath("$.userId").value(ADMIN.getId().toString()));
    }

    @Test
    void createApplication_asApplicant_shouldPassAuthenticatedUserToService() throws Exception {
        UUID raceId = UUID.randomUUID();
        ApplicationRequest request = new ApplicationRequest("Jane", "Smith", null, raceId);
        ApplicationResponse response = new ApplicationResponse(UUID.randomUUID(), "Jane", "Smith", null, raceId, APPLICANT.getId());
        when(applicationCommandService.createApplication(any(), eq(APPLICANT))).thenReturn(response);

        mockMvc.perform(post("/api/applications")
                        .with(user(APPLICANT))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    void createApplication_withoutAuth_shouldReturn401ProblemDetail() throws Exception {
        UUID raceId = UUID.randomUUID();
        ApplicationRequest request = new ApplicationRequest("John", "Doe", null, raceId);

        mockMvc.perform(post("/api/applications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void createApplication_withMissingFirstName_shouldReturn400WithFieldErrors() throws Exception {
        UUID raceId = UUID.randomUUID();
        ApplicationRequest request = new ApplicationRequest("", "Doe", null, raceId);

        mockMvc.perform(post("/api/applications")
                        .with(user(ADMIN))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.errors.firstName").exists());
    }

    @Test
    void createApplication_withMalformedJson_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .with(user(ADMIN))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deleteApplication_asAdmin_shouldReturn204() throws Exception {
        UUID appId = UUID.randomUUID();

        mockMvc.perform(delete("/api/applications/" + appId).with(user(ADMIN)).with(csrf()))
                .andExpect(status().isNoContent());

        verify(applicationCommandService).deleteApplication(eq(appId), eq(ADMIN));
    }

    @Test
    void deleteApplication_asApplicant_shouldReturn204() throws Exception {
        UUID appId = UUID.randomUUID();

        mockMvc.perform(delete("/api/applications/" + appId).with(user(APPLICANT)).with(csrf()))
                .andExpect(status().isNoContent());

        verify(applicationCommandService).deleteApplication(eq(appId), eq(APPLICANT));
    }

    @Test
    void deleteApplication_whenNotOwner_shouldReturn403ProblemDetail() throws Exception {
        UUID appId = UUID.randomUUID();
        doThrow(new AccessDeniedException("You can only delete your own applications"))
                .when(applicationCommandService).deleteApplication(eq(appId), eq(APPLICANT));

        mockMvc.perform(delete("/api/applications/" + appId).with(user(APPLICANT)).with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("You can only delete your own applications"));
    }

    @Test
    void deleteApplication_withInvalidUuid_shouldReturn400() throws Exception {
        mockMvc.perform(delete("/api/applications/not-a-uuid").with(user(ADMIN)).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
