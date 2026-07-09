package co.edu.escuelaing.techcup.logistics.security;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import co.edu.escuelaing.techcup.logistics.exception.ForbiddenRoleException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Verifica, para los metodos anotados con {@link RequireOrganizador}, que el
 * header X-User-Role (reenviado por el Gateway) indique el rol organizador.
 */
@Component
public class OrganizadorInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        if (!handlerMethod.hasMethodAnnotation(RequireOrganizador.class)) {
            return true;
        }
        String role = request.getHeader(RequestHeaders.X_USER_ROLE);
        if (role == null || !role.equalsIgnoreCase(RequestHeaders.ROLE_ORGANIZADOR)) {
            throw new ForbiddenRoleException("Esta operacion requiere el rol organizador");
        }
        return true;
    }
}
