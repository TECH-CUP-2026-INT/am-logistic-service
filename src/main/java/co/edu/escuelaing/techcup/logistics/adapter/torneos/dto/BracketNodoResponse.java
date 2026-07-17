package co.edu.escuelaing.techcup.logistics.adapter.torneos.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Proyeccion minima de un nodo del bracket de eliminacion expuesto por
 * GET /tournaments/{tournamentId}/bracket. Un equipo se considera clasificado a
 * segunda fase si ocupa alguno de los slots del bracket (o quedo resuelto como
 * ganador/perdedor de un cruce). Los slots son null mientras la ronda anterior
 * no se resuelve.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BracketNodoResponse(
        UUID slotA,
        UUID slotB,
        UUID winnerTeamId,
        UUID loserTeamId
) {

    public boolean contieneEquipo(UUID equipoId) {
        return equipoId.equals(slotA)
                || equipoId.equals(slotB)
                || equipoId.equals(winnerTeamId)
                || equipoId.equals(loserTeamId);
    }
}
