package co.edu.escuelaing.techcup.logistics.adapter.equipos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import co.edu.escuelaing.techcup.logistics.exception.IntegrationException;

class EquipoClientAdapterTest {

    private EquipoClientAdapter adapter;
    private RestClient restClient;
    private RestClient.RequestHeadersUriSpec uriSpec;
    private RestClient.ResponseSpec responseSpec;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        adapter = new EquipoClientAdapter("http://localhost:9999");
        restClient = mock(RestClient.class);
        uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);
        ReflectionTestUtils.setField(adapter, "restClient", restClient);
    }

    @SuppressWarnings("unchecked")
    @Test
    void existeEquipo_equipoExiste_retornaTrue() {
        UUID equipoId = UUID.randomUUID();

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(eq("/equipos/{id}"), any(Object[].class))).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        assertThat(adapter.existeEquipo(equipoId)).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    void existeEquipo_equipoNoExiste404_retornaFalse() {
        UUID equipoId = UUID.randomUUID();

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(eq("/equipos/{id}"), any(Object[].class))).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(
                HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                        HttpHeaders.EMPTY, new byte[0], null));

        assertThat(adapter.existeEquipo(equipoId)).isFalse();
    }

    @SuppressWarnings("unchecked")
    @Test
    void existeEquipo_errorHttpDistintoDe404_lanzaIntegrationException() {
        UUID equipoId = UUID.randomUUID();

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(eq("/equipos/{id}"), any(Object[].class))).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(
                HttpClientErrorException.create(HttpStatus.SERVICE_UNAVAILABLE, "Unavailable",
                        HttpHeaders.EMPTY, new byte[0], null));

        assertThatThrownBy(() -> adapter.existeEquipo(equipoId))
                .isInstanceOf(IntegrationException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    void existeEquipo_errorInesperado_lanzaIntegrationException() {
        UUID equipoId = UUID.randomUUID();

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(eq("/equipos/{id}"), any(Object[].class))).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> adapter.existeEquipo(equipoId))
                .isInstanceOf(IntegrationException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    void existeJugadorEnEquipo_existe_retornaTrue() {
        UUID jugadorId = UUID.randomUUID();
        UUID equipoId = UUID.randomUUID();

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(eq("/equipos/{equipoId}/jugadores/{jugadorId}"), any(Object[].class)))
                .thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        assertThat(adapter.existeJugadorEnEquipo(jugadorId, equipoId)).isTrue();
    }
}
