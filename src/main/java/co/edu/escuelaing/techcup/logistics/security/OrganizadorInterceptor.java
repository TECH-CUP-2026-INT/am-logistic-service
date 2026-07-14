package co.edu.escuelaing.techcup.logistics.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import co.edu.escuelaing.techcup.logistics.exception.ForbiddenRoleException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Verifica, para los metodos anotados con {@link RequireOrganizador}, que el usuario
 * autenticado (a partir de los claims del JWT ya validado por el Gateway, ver
 * {@link JwtClaimsFilter}) tenga el rol organizador. Ya no confia en un header sin
 * validar: la autoridad viene del SecurityContext poblado por el filtro JWT.
 */
@Component
public class OrganizadorInterceptor implements HandlerInterceptor {

    private static final String ROLE_ORGANIZADOR_AUTHORITY = "ROLE_ORGANIZADOR";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        if (!handlerMethod.hasMethodAnnotation(RequireOrganizador.class)) {
            return true;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isOrganizador = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(ROLE_ORGANIZADOR_AUTHORITY));
        if (!isOrganizador) {
            throw new ForbiddenRoleException("Esta operacion requiere el rol organizador");
        }
        return true;
    }
}
