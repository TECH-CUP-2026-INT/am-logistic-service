package co.edu.escuelaing.techcup.logistics.adapter.torneos;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import co.edu.escuelaing.techcup.logistics.adapter.torneos.dto.PartidoDTO;
import co.edu.escuelaing.techcup.logistics.exception.IntegrationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador REST sincrono hacia el Servicio de Torneos.
 *
 * Nota: contrato pendiente de confirmar con el equipo de Torneos:
 *   - Ruta real para consultar un partido por id (se asume GET /partidos/{id}).
 *   - Forma exacta del payload de respuesta (se asume id, jornadaId, fechaProgramada).
 *   - Codigo de error para "partido no existe" (se asume 404).
 */
@Component
@Slf4j
public class TorneoClientAdapter implements TorneoClientPort {

    private final RestClient restClient;

    public TorneoClientAdapter(@Value("${logistics.integration.torneos.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public Optional<PartidoDTO> obtenerPartido(UUID partidoId) {
        try {
            PartidoDTO partido = restClient.get()
                    .uri("/partidos/{id}", partidoId)
                    .retrieve()
                    .body(PartidoDTO.class);
            return Optional.ofNullable(partido);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            log.warn("Fallo al consultar el partido {} en Torneos: {}", partidoId, e.getMessage());
            throw new IntegrationException("No fue posible validar el partido con el Servicio de Torneos", e);
        } catch (Exception e) {
            log.warn("Fallo al consultar el partido {} en Torneos: {}", partidoId, e.getMessage());
            throw new IntegrationException("No fue posible validar el partido con el Servicio de Torneos", e);
        }
    }

    @Override
    public boolean existePartido(UUID partidoId) {
        return obtenerPartido(partidoId).isPresent();
    }
}
