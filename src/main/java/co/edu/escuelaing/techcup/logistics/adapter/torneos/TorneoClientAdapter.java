package co.edu.escuelaing.techcup.logistics.adapter.torneos;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import co.edu.escuelaing.techcup.logistics.adapter.torneos.dto.BracketNodoResponse;
import co.edu.escuelaing.techcup.logistics.adapter.torneos.dto.TorneoResponse;
import co.edu.escuelaing.techcup.logistics.exception.IntegrationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador REST sincrono hacia el Servicio de Torneos (mk-tournament-service).
 *
 * Endpoints reales consumidos:
 *   - GET /tournaments/by-match/{matchId}       -> 200 TournamentResponse | 404 (MatchupNotFound)
 *   - GET /tournaments/{tournamentId}/bracket   -> 200 List&lt;BracketNode&gt;
 *
 * "Clasificado a segunda fase" se resuelve verificando que el equipo aparezca en
 * el bracket de eliminacion del torneo al que pertenece el partido.
 */
@Component
@Slf4j
public class TorneoClientAdapter implements TorneoClientPort {

    private static final ParameterizedTypeReference<List<BracketNodoResponse>> BRACKET_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public TorneoClientAdapter(@Value("${logistics.integration.torneos.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public boolean existePartido(UUID partidoId) {
        return obtenerTorneoIdPorPartido(partidoId).isPresent();
    }

    @Override
    public boolean equipoClasificadoSegundaFase(UUID partidoId, UUID equipoId) {
        return obtenerTorneoIdPorPartido(partidoId)
                .map(this::obtenerBracket)
                .map(bracket -> bracket.stream().anyMatch(nodo -> nodo.contieneEquipo(equipoId)))
                .orElse(false);
    }

    /**
     * Resuelve el torneo al que pertenece un partido. Devuelve vacio si Torneos
     * responde 404 (el partido no existe); cualquier otro fallo se propaga como
     * IntegrationException.
     */
    private Optional<UUID> obtenerTorneoIdPorPartido(UUID partidoId) {
        try {
            TorneoResponse torneo = restClient.get()
                    .uri("/tournaments/by-match/{matchId}", partidoId)
                    .retrieve()
                    .body(TorneoResponse.class);
            return Optional.ofNullable(torneo).map(TorneoResponse::id);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw integrationFailure("consultar el torneo del partido " + partidoId, e);
        } catch (Exception e) {
            throw integrationFailure("consultar el torneo del partido " + partidoId, e);
        }
    }

    private List<BracketNodoResponse> obtenerBracket(UUID tournamentId) {
        try {
            List<BracketNodoResponse> bracket = restClient.get()
                    .uri("/tournaments/{tournamentId}/bracket", tournamentId)
                    .retrieve()
                    .body(BRACKET_TYPE);
            return bracket != null ? bracket : List.of();
        } catch (RestClientResponseException e) {
            throw integrationFailure("consultar el bracket del torneo " + tournamentId, e);
        } catch (Exception e) {
            throw integrationFailure("consultar el bracket del torneo " + tournamentId, e);
        }
    }

    private IntegrationException integrationFailure(String accion, Exception e) {
        log.warn("Fallo al {} en Torneos: {}", accion, e.getMessage());
        return new IntegrationException("No fue posible validar el dato con el Servicio de Torneos", e);
    }
}
