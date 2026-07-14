package co.edu.escuelaing.techcup.logistics.exception;

/**
 * Se lanza cuando una llamada a un microservicio externo (Torneos, Equipos)
 * falla de forma inesperada (no un simple "no encontrado") y el dato es
 * indispensable para continuar la operacion actual.
 */
public class IntegrationException extends RuntimeException {

    public IntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
