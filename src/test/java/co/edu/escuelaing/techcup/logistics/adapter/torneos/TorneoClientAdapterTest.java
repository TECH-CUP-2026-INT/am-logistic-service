package co.edu.escuelaing.techcup.logistics.adapter.torneos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import co.edu.escuelaing.techcup.logistics.adapter.torneos.dto.PartidoDTO;
import co.edu.escuelaing.techcup.logistics.exception.IntegrationException;

class TorneoClientAdapterTest {

    private TorneoClientAdapter adapter;
    private RestClient restClient;
    private RestClient.RequestHeadersUriSpec uriSpec;
    private RestClient.ResponseSpec responseSpec;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        adapter = new TorneoClientAdapter("http://localhost:9999");
        restClient = mock(RestClient.class);
        uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);
        ReflectionTestUtils.setField(adapter, "restClient", restClient);
    }

    @SuppressWarnings("unchecked")
    @Test
    void obtenerPartido_partidoExiste_retornaOptionalConDatos() {
        UUID partidoId = UUID.randomUUID();
        PartidoDTO dto = new PartidoDTO(partidoId, UUID.randomUUID(), Instant.now());

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(eq("/partidos/{id}"), any(Object[].class))).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(PartidoDTO.class)).thenReturn(dto);

        Optional<PartidoDTO> result = adapter.obtenerPartido(partidoId);

        assertThat(result).contains(dto);
    }

    @SuppressWarnings("unchecked")
    @Test
    void obtenerPartido_partidoNoExiste404_retornaOptionalVacio() {
        UUID partidoId = UUID.randomUUID();

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(eq("/partidos/{id}"), any(Object[].class))).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(PartidoDTO.class)).thenThrow(
                HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                        HttpHeaders.EMPTY, new byte[0], null));

        Optional<PartidoDTO> result = adapter.obtenerPartido(partidoId);

        assertThat(result).isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    void obtenerPartido_errorHttpDistintoDe404_lanzaIntegrationException() {
        UUID partidoId = UUID.randomUUID();

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(eq("/partidos/{id}"), any(Object[].class))).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(PartidoDTO.class)).thenThrow(
                HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error",
                        HttpHeaders.EMPTY, new byte[0], null));

        assertThatThrownBy(() -> adapter.obtenerPartido(partidoId))
                .isInstanceOf(IntegrationException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    void obtenerPartido_errorInesperado_lanzaIntegrationException() {
        UUID partidoId = UUID.randomUUID();

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(eq("/partidos/{id}"), any(Object[].class))).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(PartidoDTO.class)).thenThrow(new RuntimeException("timeout"));

        assertThatThrownBy(() -> adapter.obtenerPartido(partidoId))
                .isInstanceOf(IntegrationException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    void existePartido_partidoExiste_retornaTrue() {
        UUID partidoId = UUID.randomUUID();
        PartidoDTO dto = new PartidoDTO(partidoId, UUID.randomUUID(), Instant.now());

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(eq("/partidos/{id}"), any(Object[].class))).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(PartidoDTO.class)).thenReturn(dto);

        assertThat(adapter.existePartido(partidoId)).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    void existePartido_partidoNoExiste_retornaFalse() {
        UUID partidoId = UUID.randomUUID();

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(eq("/partidos/{id}"), any(Object[].class))).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(PartidoDTO.class)).thenThrow(
                HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                        HttpHeaders.EMPTY, new byte[0], null));

        assertThat(adapter.existePartido(partidoId)).isFalse();
    }

    @SuppressWarnings("unchecked")
    @Test
    void equipoClasificadoSegundaFase_clasificado_retornaTrue() {
        UUID equipoId = UUID.randomUUID();

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(eq("/equipos/{equipoId}/clasificacion/segunda-fase"), any(Object[].class)))
                .thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(org.springframework.http.ResponseEntity.ok().build());

        assertThat(adapter.equipoClasificadoSegundaFase(equipoId)).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    void equipoClasificadoSegundaFase_noClasificado404_retornaFalse() {
        UUID equipoId = UUID.randomUUID();

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(eq("/equipos/{equipoId}/clasificacion/segunda-fase"), any(Object[].class)))
                .thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(
                HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                        HttpHeaders.EMPTY, new byte[0], null));

        assertThat(adapter.equipoClasificadoSegundaFase(equipoId)).isFalse();
    }

    @SuppressWarnings("unchecked")
    @Test
    void equipoClasificadoSegundaFase_errorHttpDistintoDe404_lanzaIntegrationException() {
        UUID equipoId = UUID.randomUUID();

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(eq("/equipos/{equipoId}/clasificacion/segunda-fase"), any(Object[].class)))
                .thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(
                HttpClientErrorException.create(HttpStatus.SERVICE_UNAVAILABLE, "Unavailable",
                        HttpHeaders.EMPTY, new byte[0], null));

        assertThatThrownBy(() -> adapter.equipoClasificadoSegundaFase(equipoId))
                .isInstanceOf(IntegrationException.class);
    }
}
