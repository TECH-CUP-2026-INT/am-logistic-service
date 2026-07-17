package co.edu.escuelaing.techcup.logistics.dto.response;

import java.time.Instant;
import java.util.UUID;

import co.edu.escuelaing.techcup.logistics.enums.EstadoDotacion;
import co.edu.escuelaing.techcup.logistics.enums.TipoItemDotacion;

public record ItemDotacionResponse(
        UUID id,
        UUID arbitroId,
        TipoItemDotacion tipoItem,
        EstadoDotacion estado,
        UUID responsableAsignadoId,
        UUID entregadoPorId,
        Instant fechaRegistro,
        Instant fechaEntrega,
        UUID recibidoPorId,
        Instant fechaDevolucion,
        String observaciones
) {
}
