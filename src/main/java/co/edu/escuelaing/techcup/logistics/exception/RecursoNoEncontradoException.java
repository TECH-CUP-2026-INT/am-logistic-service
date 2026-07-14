package co.edu.escuelaing.techcup.logistics.exception;

/**
 * Se lanza cuando un recurso propio (definicion, entrega, item de dotacion) o
 * un dato externo referenciado (partido, equipo, jugador) no existe.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
