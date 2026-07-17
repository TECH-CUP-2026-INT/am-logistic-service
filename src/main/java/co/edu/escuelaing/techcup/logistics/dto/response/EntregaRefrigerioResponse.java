package co.edu.escuelaing.techcup.logistics.dto.response;

import java.time.Instant;
import java.util.UUID;

public record EntregaRefrigerioResponse(
        UUID id,
        UUID definicionRefrigerioId,
        UUID partidoId,
        UUID equipoId,
        UUID capitanId,
        UUID responsableId,
        Instant fechaEntrega,
        String observaciones
) {
}
