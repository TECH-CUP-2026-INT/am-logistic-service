package co.edu.escuelaing.techcup.logistics.exception;

/**
 * Se lanza cuando el usuario autenticado no tiene el rol requerido (organizador)
 * o falta la identidad esperada en los headers reenviados por el Gateway.
 */
public class ForbiddenRoleException extends RuntimeException {

    public ForbiddenRoleException(String message) {
        super(message);
    }
}
