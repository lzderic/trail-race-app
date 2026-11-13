package com.race.command.service;

import com.race.command.dto.LoginRequest;
import com.race.command.dto.LoginResponse;
import com.race.command.enums.Role;
import com.race.command.security.AuthenticatedUser;
import com.race.command.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private AuthenticatedUser adminUser;

    @BeforeEach
    void setUp() {
        adminUser = new AuthenticatedUser(
                UUID.randomUUID(), "admin@trailrace.com", "Admin", "User", Role.ADMINISTRATOR, "hashed");
    }

    @Test
    void login_withValidCredentials_shouldReturnToken() {
        when(authenticationManager.authenticate(any())).thenReturn(authenticated(adminUser));
        when(jwtService.generateToken(eq(adminUser), anyMap())).thenReturn("jwt-token-123");

        LoginResponse response = authService.login(new LoginRequest("admin@trailrace.com", "admin123"));

        assertThat(response.token()).isEqualTo("jwt-token-123");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void login_shouldIncludeIdentityClaimsInToken() {
        when(authenticationManager.authenticate(any())).thenReturn(authenticated(adminUser));
        when(jwtService.generateToken(eq(adminUser), anyMap())).thenReturn("jwt");

        authService.login(new LoginRequest("admin@trailrace.com", "admin123"));

        ArgumentCaptor<Map<String, Object>> claims = ArgumentCaptor.forClass(Map.class);
        verify(jwtService).generateToken(eq(adminUser), claims.capture());
        assertThat(claims.getValue())
                .containsEntry(AuthService.CLAIM_ID, adminUser.getId().toString())
                .containsEntry(AuthService.CLAIM_ROLE, "ADMINISTRATOR")
                .containsEntry(AuthService.CLAIM_FIRST_NAME, "Admin")
                .containsEntry(AuthService.CLAIM_LAST_NAME, "User");
    }

    @Test
    void login_withMissingNames_shouldUseEmptyClaimsInsteadOfNull() {
        AuthenticatedUser nameless = new AuthenticatedUser(
                UUID.randomUUID(), "x@example.com", null, null, Role.APPLICANT, "hashed");
        when(authenticationManager.authenticate(any())).thenReturn(authenticated(nameless));
        when(jwtService.generateToken(eq(nameless), anyMap())).thenReturn("jwt");

        authService.login(new LoginRequest("x@example.com", "pw"));

        verify(jwtService).generateToken(eq(nameless), eq(Map.<String, Object>of(
                AuthService.CLAIM_ID, nameless.getId().toString(),
                AuthService.CLAIM_ROLE, "APPLICANT",
                AuthService.CLAIM_FIRST_NAME, "",
                AuthService.CLAIM_LAST_NAME, "")));
    }

    @Test
    void login_withInvalidCredentials_shouldThrowBadCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("wrong@email.com", "bad")))
                .isInstanceOf(BadCredentialsException.class);
    }

    private static Authentication authenticated(AuthenticatedUser user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }
}
