package co.edu.escuelaing.techcup.logistics.dto.request;

import jakarta.validation.constraints.Size;

/**
 * El entregadoPorId no viaja en el body: es el organizador autenticado
 * (header X-User-Id) quien marca la entrega.
 */
public record MarcarDotacionEntregadaRequest(

        @Size(max = 500)
        String observaciones
) {
}
