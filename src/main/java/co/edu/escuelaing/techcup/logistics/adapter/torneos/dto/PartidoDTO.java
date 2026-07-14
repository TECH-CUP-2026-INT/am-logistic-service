package co.edu.escuelaing.techcup.logistics.adapter.torneos.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Representacion minima de un partido segun el Servicio de Torneos.
 * TODO: confirmar con el equipo de Torneos la forma real del payload expuesto.
 */
public record PartidoDTO(
        UUID id,
        UUID jornadaId,
        Instant fechaProgramada
) {
}
