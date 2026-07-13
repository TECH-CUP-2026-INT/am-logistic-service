package co.edu.escuelaing.techcup.logistics.security;

import java.util.UUID;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Punto único para obtener el id del usuario autenticado a partir del JWT ya validado
 * por el API Gateway (reemplaza la lectura directa, no verificada, del header
 * X-User-Id).
 */
@Component
public class CurrentUserProvider {

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new InsufficientAuthenticationException("No hay un usuario autenticado en el contexto de seguridad");
        }
        return user.userId();
    }
}
