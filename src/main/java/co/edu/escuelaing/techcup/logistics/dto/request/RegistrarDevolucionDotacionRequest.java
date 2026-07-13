package co.edu.escuelaing.techcup.logistics.dto.request;

import jakarta.validation.constraints.Size;

/**
 * El recibidoPorId no viaja en el body: es el organizador autenticado
 * (JWT) quien registra la devolucion hecha por el arbitro.
 */
public record RegistrarDevolucionDotacionRequest(

        @Size(max = 500)
        String observaciones
) {
}
