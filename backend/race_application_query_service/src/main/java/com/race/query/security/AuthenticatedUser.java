package com.race.query.security;

import com.race.query.entity.User;
import com.race.query.enums.Role;
import lombok.Getter;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Security principal for an authenticated user.
 * <p>
 * Carries the identity details (id, role, name) that controllers and services need,
 * so they never have to re-query the database or reach into the {@code SecurityContextHolder}.
 */
@Getter
public final class AuthenticatedUser implements UserDetails, CredentialsContainer {

    private static final String ROLE_PREFIX = "ROLE_";

    private final UUID id;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final Role role;
    private String password;

    public AuthenticatedUser(UUID id, String email, String firstName, String lastName, Role role, String password) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.password = password;
    }

    public static AuthenticatedUser from(User user) {
        return new AuthenticatedUser(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getPassword()
        );
    }

    public boolean isAdministrator() {
        return role == Role.ADMINISTRATOR;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(ROLE_PREFIX + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}
