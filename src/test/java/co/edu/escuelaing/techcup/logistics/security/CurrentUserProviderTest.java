package co.edu.escuelaing.techcup.logistics.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class CurrentUserProviderTest {

    private final CurrentUserProvider provider = new CurrentUserProvider();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserId_withAuthenticatedUserPrincipal_returnsUserId() {
        UUID userId = UUID.randomUUID();
        AuthenticatedUser principal = new AuthenticatedUser(userId, Set.of("ORGANIZADOR"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, java.util.List.of()));

        UUID result = provider.getCurrentUserId();

        assertThat(result).isEqualTo(userId);
    }

    @Test
    void getCurrentUserId_withoutAuthentication_throws() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(provider::getCurrentUserId)
                .isInstanceOf(InsufficientAuthenticationException.class);
    }

    @Test
    void getCurrentUserId_withWrongPrincipalType_throws() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new InternalServicePrincipal(), null, java.util.List.of()));

        assertThatThrownBy(provider::getCurrentUserId)
                .isInstanceOf(InsufficientAuthenticationException.class);
    }
}
