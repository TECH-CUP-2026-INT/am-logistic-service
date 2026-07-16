package co.edu.escuelaing.techcup.logistics.security;

/**
 * Principal para llamadas servicio-a-servicio autenticadas con la API key interna.
 * Deliberadamente no tiene userId: nunca debe poder pasar por CurrentUserProvider.
 */
public record InternalServicePrincipal() {

    public static final InternalServicePrincipal INSTANCE = new InternalServicePrincipal();
}
