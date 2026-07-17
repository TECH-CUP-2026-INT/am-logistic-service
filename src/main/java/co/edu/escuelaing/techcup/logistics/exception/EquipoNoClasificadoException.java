package co.edu.escuelaing.techcup.logistics.exception;

/**
 * Se lanza cuando se intenta definir un refrigerio para un equipo que, segun
 * los resultados registrados en el Servicio de Torneos, aun no ha clasificado
 * a segunda fase.
 */
public class EquipoNoClasificadoException extends RuntimeException {

    public EquipoNoClasificadoException(String message) {
        super(message);
    }
}
