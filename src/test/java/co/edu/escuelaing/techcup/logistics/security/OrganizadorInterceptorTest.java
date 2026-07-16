package co.edu.escuelaing.techcup.logistics.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;

import co.edu.escuelaing.techcup.logistics.exception.ForbiddenRoleException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class OrganizadorInterceptorTest {

    private final OrganizadorInterceptor interceptor = new OrganizadorInterceptor();
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void preHandle_notHandlerMethod_passesThrough() {
        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }

    @Test
    void preHandle_methodWithoutAnnotation_passesThrough() throws NoSuchMethodException {
        HandlerMethod handlerMethod = new HandlerMethod(new SampleController(),
                SampleController.class.getMethod("sinAnotacion"));

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertThat(result).isTrue();
    }

    @Test
    void preHandle_authenticatedWithOrganizadorRole_passes() throws NoSuchMethodException {
        HandlerMethod handlerMethod = new HandlerMethod(new SampleController(),
                SampleController.class.getMethod("requiereOrganizador"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", null,
                        List.of(new SimpleGrantedAuthority("ROLE_ORGANIZADOR"))));

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertThat(result).isTrue();
    }

    @Test
    void preHandle_authenticatedWithoutOrganizadorRole_throwsForbidden() throws NoSuchMethodException {
        HandlerMethod handlerMethod = new HandlerMethod(new SampleController(),
                SampleController.class.getMethod("requiereOrganizador"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", null,
                        List.of(new SimpleGrantedAuthority("ROLE_SERVICIO_INTERNO"))));

        assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
                .isInstanceOf(ForbiddenRoleException.class);
    }

    @Test
    void preHandle_unauthenticated_throwsForbidden() throws NoSuchMethodException {
        HandlerMethod handlerMethod = new HandlerMethod(new SampleController(),
                SampleController.class.getMethod("requiereOrganizador"));
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
                .isInstanceOf(ForbiddenRoleException.class);
    }

    static class SampleController {
        public void sinAnotacion() {
            // Cuerpo vacio a proposito: el metodo solo se usa como referencia
            // reflexiva en HandlerMethod, nunca se invoca.
        }

        @RequireOrganizador
        public void requiereOrganizador() {
            // Cuerpo vacio a proposito: el metodo solo se usa como referencia
            // reflexiva en HandlerMethod, nunca se invoca.
        }
    }
}
