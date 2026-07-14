package co.edu.escuelaing.techcup.logistics.exception;

/**
 * Se lanza ante cualquier intento de duplicar un registro que el dominio
 * considera unico: una definicion de refrigerio para el mismo partido/equipo,
 * una entrega para el mismo destinatario en el mismo partido, o marcar como
 * entregado un item de dotacion que ya lo estaba.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
