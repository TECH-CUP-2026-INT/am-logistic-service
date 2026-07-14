package co.edu.escuelaing.techcup.logistics.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DefinicionRefrigerioResponse(
        UUID id,
        UUID partidoId,
        UUID equipoId,
        List<ItemRefrigerioResponse> items,
        String observaciones,
        UUID creadoPorId,
        Instant fechaCreacion
) {
}
