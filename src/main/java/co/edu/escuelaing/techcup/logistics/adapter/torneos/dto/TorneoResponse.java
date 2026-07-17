package co.edu.escuelaing.techcup.logistics.adapter.torneos.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Proyeccion minima de la respuesta de GET /tournaments/by-match/{matchId}.
 * Solo consumimos el id del torneo para poder consultar su bracket; el resto
 * de campos de TournamentResponse se ignoran.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TorneoResponse(UUID id) {
}
