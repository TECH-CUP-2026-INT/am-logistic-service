package co.edu.escuelaing.techcup.logistics.dto.response;

import java.time.Instant;
import java.util.UUID;

import co.edu.escuelaing.techcup.logistics.enums.TipoDestinatario;

public record EntregaRefrigerioResponse(
        UUID id,
        UUID definicionRefrigerioId,
        UUID partidoId,
        TipoDestinatario tipoDestinatario,
        UUID destinatarioId,
        UUID responsableId,
        Instant fechaEntrega,
        String observaciones
) {
}
