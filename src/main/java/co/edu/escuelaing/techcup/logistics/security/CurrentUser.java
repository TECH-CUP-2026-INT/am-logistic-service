package co.edu.escuelaing.techcup.logistics.security;

import java.util.UUID;

import co.edu.escuelaing.techcup.logistics.exception.ForbiddenRoleException;

/**
 * Extrae la identidad del usuario autenticado a partir del header X-User-Id
 * que reenvia el API Gateway (el JWT ya fue validado alli).
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static UUID idFrom(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            throw new ForbiddenRoleException("Falta el header " + RequestHeaders.X_USER_ID);
        }
        try {
            return UUID.fromString(headerValue);
        } catch (IllegalArgumentException e) {
            throw new ForbiddenRoleException("El header " + RequestHeaders.X_USER_ID + " no es un UUID valido");
        }
    }
}
