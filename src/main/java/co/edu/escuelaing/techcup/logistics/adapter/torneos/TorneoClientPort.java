package co.edu.escuelaing.techcup.logistics.adapter.torneos;

import java.util.UUID;

/**
 * Puerto hacia el Servicio de Torneos (mk-tournament-service): permite validar
 * que un partido existe y que un equipo clasifico a segunda fase antes de
 * definir o registrar una entrega de refrigerios.
 *
 * El contrato se ajusta a los endpoints reales expuestos por Torneos:
 *   - GET /tournaments/by-match/{matchId}  -> 200 TournamentResponse | 404
 *   - GET /tournaments/{tournamentId}/bracket -> 200 List&lt;BracketNode&gt;
 */
public interface TorneoClientPort {

    /**
     * Indica si el partido existe en Torneos. Se resuelve consultando el torneo
     * asociado al partido (GET /tournaments/by-match/{matchId}); un 404 significa
     * que el partido no esta registrado.
     */
    boolean existePartido(UUID partidoId);

    /**
     * Indica si el equipo ya clasifico a segunda fase, es decir, si aparece en el
     * bracket de eliminacion del torneo al que pertenece el partido. Los refrigerios
     * solo pueden definirse para equipos que cumplan esta condicion.
     */
    boolean equipoClasificadoSegundaFase(UUID partidoId, UUID equipoId);
}
