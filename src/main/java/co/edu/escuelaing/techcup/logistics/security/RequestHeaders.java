package co.edu.escuelaing.techcup.logistics.security;

/**
 * Nombres de los headers que reenvia el API Gateway con la identidad ya
 * validada del usuario autenticado (el JWT se valida alli, no en este servicio).
 */
public final class RequestHeaders {

    public static final String X_USER_ID = "X-User-Id";
    public static final String X_USER_ROLE = "X-User-Role";
    public static final String ROLE_ORGANIZADOR = "organizador";

    private RequestHeaders() {
    }
}
