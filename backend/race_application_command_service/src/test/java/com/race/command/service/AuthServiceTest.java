package com.race.command.service;
import com.race.command.dto.LoginRequest;
import com.race.command.dto.LoginResponse;
import com.race.command.entity.User;
import com.race.command.enums.Role;
import com.race.command.repository.UserRepository;
import com.race.command.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private UserRepository userRepository;
    @InjectMocks private AuthService authService;
    private User adminUser;
    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(UUID.randomUUID()).firstName("Admin").lastName("User")
                .email("admin@trailrace.com").role(Role.ADMINISTRATOR).password("hashed").build();
    }
    @Test
    void login_withValidCredentials_shouldReturnToken() {
        var springUser = new org.springframework.security.core.userdetails.User(
                "admin@trailrace.com", "hashed",
                List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR")));
        Authentication auth = new UsernamePasswordAuthenticationToken(springUser, null, springUser.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByEmail("admin@trailrace.com")).thenReturn(Optional.of(adminUser));
        when(jwtService.generateToken(any(), anyMap())).thenReturn("jwt-token-123");
        LoginResponse response = authService.login(new LoginRequest("admin@trailrace.com", "admin123"));
        assertThat(response.token()).isEqualTo("jwt-token-123");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
    @Test
    void login_withInvalidCredentials_shouldThrowBadCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));
        assertThatThrownBy(() -> authService.login(new LoginRequest("wrong@email.com", "bad")))
                .isInstanceOf(BadCredentialsException.class);
    }
    @Test
    void login_whenUserNotFoundAfterAuth_shouldThrowUsernameNotFound() {
        var springUser = new org.springframework.security.core.userdetails.User(
                "ghost@trailrace.com", "hashed",
                List.of(new SimpleGrantedAuthority("ROLE_APPLICANT")));
        Authentication auth = new UsernamePasswordAuthenticationToken(springUser, null, springUser.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByEmail("ghost@trailrace.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.login(new LoginRequest("ghost@trailrace.com", "pass")))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
