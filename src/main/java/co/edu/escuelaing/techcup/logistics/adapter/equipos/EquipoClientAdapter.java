package co.edu.escuelaing.techcup.logistics.adapter.equipos;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import co.edu.escuelaing.techcup.logistics.exception.IntegrationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador REST sincrono hacia el Servicio de Equipos.
 *
 * Nota: contrato pendiente de confirmar con el equipo de Equipos:
 *   - Ruta real para validar un equipo (se asume GET /equipos/{id}).
 *   - Ruta real para validar pertenencia de un jugador a un equipo
 *     (se asume GET /equipos/{equipoId}/jugadores/{jugadorId}).
 *   - Codigo de error para "no existe" (se asume 404).
 */
@Component
@Slf4j
public class EquipoClientAdapter implements EquipoClientPort {

    private final RestClient restClient;

    public EquipoClientAdapter(@Value("${logistics.integration.equipos.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public boolean existeEquipo(UUID equipoId) {
        return existe("/equipos/{id}", equipoId);
    }

    @Override
    public boolean existeJugadorEnEquipo(UUID jugadorId, UUID equipoId) {
        return existe("/equipos/{equipoId}/jugadores/{jugadorId}", equipoId, jugadorId);
    }

    private boolean existe(String uri, Object... uriVariables) {
        try {
            restClient.get()
                    .uri(uri, uriVariables)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return false;
            }
            log.warn("Fallo al consultar el Servicio de Equipos ({}): {}", uri, e.getMessage());
            throw new IntegrationException("No fue posible validar el dato con el Servicio de Equipos", e);
        } catch (Exception e) {
            log.warn("Fallo al consultar el Servicio de Equipos ({}): {}", uri, e.getMessage());
            throw new IntegrationException("No fue posible validar el dato con el Servicio de Equipos", e);
        }
    }
}
