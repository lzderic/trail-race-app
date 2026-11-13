package com.race.query.service;

import com.race.query.dto.LoginRequest;
import com.race.query.dto.LoginResponse;
import com.race.query.security.AuthenticatedUser;
import com.race.query.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    public static final String CLAIM_ID = "id";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_FIRST_NAME = "firstName";
    public static final String CLAIM_LAST_NAME = "lastName";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        String token = jwtService.generateToken(user, buildClaims(user));
        log.info("User logged in: {}", user.getEmail());
        return new LoginResponse(token);
    }

    private static Map<String, Object> buildClaims(AuthenticatedUser user) {
        return Map.of(
                CLAIM_ID, user.getId().toString(),
                CLAIM_ROLE, user.getRole().name(),
                CLAIM_FIRST_NAME, Objects.requireNonNullElse(user.getFirstName(), ""),
                CLAIM_LAST_NAME, Objects.requireNonNullElse(user.getLastName(), "")
        );
    }
}
