package com.race.query.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_shouldReturn404() {
        UUID id = UUID.randomUUID();
        ResourceNotFoundException ex = new ResourceNotFoundException("Race", id);

        ProblemDetail pd = handler.handleNotFound(ex);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(pd.getDetail()).contains("Race").contains(id.toString());
    }

    @Test
    void handleTypeMismatch_shouldReturn400NamingTheParameter() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "not-a-uuid", UUID.class, "id", null, new IllegalArgumentException("bad uuid"));

        ProblemDetail pd = handler.handleTypeMismatch(ex);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(pd.getDetail()).contains("not-a-uuid").contains("id");
    }

    @Test
    void handleUnreadableBody_shouldReturn400() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "JSON parse error", new MockHttpInputMessage(new byte[0]));

        ProblemDetail pd = handler.handleUnreadableBody(ex);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(pd.getDetail()).isEqualTo("Malformed request body");
    }

    @Test
    void handleAuthentication_withBadCredentials_shouldReturn401() {
        ProblemDetail pd = handler.handleAuthentication(new BadCredentialsException("Bad credentials"));

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(pd.getDetail()).isEqualTo("Invalid email or password");
    }

    @Test
    void handleAccessDenied_shouldReturn403() {
        ProblemDetail pd = handler.handleAccessDenied(new AccessDeniedException("You can only view your own applications"));

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(pd.getDetail()).isEqualTo("You can only view your own applications");
    }

    @Test
    void handleGeneral_shouldReturn500() {
        Exception ex = new RuntimeException("boom");

        ProblemDetail pd = handler.handleGeneral(ex);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(pd.getDetail()).isEqualTo("An unexpected error occurred");
    }

    @Test
    void resourceNotFoundException_message() {
        UUID id = UUID.randomUUID();
        ResourceNotFoundException ex = new ResourceNotFoundException("Application", id);
        assertThat(ex.getMessage()).isEqualTo("Application not found with id: " + id);
    }
}
