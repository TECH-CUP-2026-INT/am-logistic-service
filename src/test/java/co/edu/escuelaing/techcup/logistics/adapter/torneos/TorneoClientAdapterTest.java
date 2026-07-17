package co.edu.escuelaing.techcup.logistics.adapter.torneos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import co.edu.escuelaing.techcup.logistics.adapter.torneos.dto.BracketNodoResponse;
import co.edu.escuelaing.techcup.logistics.adapter.torneos.dto.TorneoResponse;
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
    private void stubByMatch() {
        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(eq("/tournaments/by-match/{matchId}"), any(Object[].class))).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
    }

    @SuppressWarnings("unchecked")
    private void stubByMatchAndBracket() {
        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(eq("/tournaments/by-match/{matchId}"), any(Object[].class))).thenReturn(uriSpec);
        when(uriSpec.uri(eq("/tournaments/{tournamentId}/bracket"), any(Object[].class))).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
    }

    private HttpClientErrorException notFound() {
        return HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                HttpHeaders.EMPTY, new byte[0], null);
    }

    @Test
    void existePartido_partidoExiste_retornaTrue() {
        stubByMatch();
        when(responseSpec.body(TorneoResponse.class)).thenReturn(new TorneoResponse(UUID.randomUUID()));

        assertThat(adapter.existePartido(UUID.randomUUID())).isTrue();
    }

    @Test
    void existePartido_partidoNoExiste404_retornaFalse() {
        stubByMatch();
        when(responseSpec.body(TorneoResponse.class)).thenThrow(notFound());

        assertThat(adapter.existePartido(UUID.randomUUID())).isFalse();
    }

    @Test
    void existePartido_errorHttpDistintoDe404_lanzaIntegrationException() {
        stubByMatch();
        when(responseSpec.body(TorneoResponse.class)).thenThrow(
                HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error",
                        HttpHeaders.EMPTY, new byte[0], null));

        assertThatThrownBy(() -> adapter.existePartido(UUID.randomUUID()))
                .isInstanceOf(IntegrationException.class);
    }

    @Test
    void existePartido_errorInesperado_lanzaIntegrationException() {
        stubByMatch();
        when(responseSpec.body(TorneoResponse.class)).thenThrow(new RuntimeException("timeout"));

        assertThatThrownBy(() -> adapter.existePartido(UUID.randomUUID()))
                .isInstanceOf(IntegrationException.class);
    }

    @Test
    void equipoClasificadoSegundaFase_equipoEnBracket_retornaTrue() {
        UUID torneoId = UUID.randomUUID();
        UUID equipoId = UUID.randomUUID();
        stubByMatchAndBracket();
        when(responseSpec.body(TorneoResponse.class)).thenReturn(new TorneoResponse(torneoId));
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(List.of(
                new BracketNodoResponse(UUID.randomUUID(), UUID.randomUUID(), null, null),
                new BracketNodoResponse(equipoId, UUID.randomUUID(), null, null)));

        assertThat(adapter.equipoClasificadoSegundaFase(UUID.randomUUID(), equipoId)).isTrue();
    }

    @Test
    void equipoClasificadoSegundaFase_equipoFueraDelBracket_retornaFalse() {
        UUID torneoId = UUID.randomUUID();
        stubByMatchAndBracket();
        when(responseSpec.body(TorneoResponse.class)).thenReturn(new TorneoResponse(torneoId));
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(List.of(
                new BracketNodoResponse(UUID.randomUUID(), UUID.randomUUID(), null, null)));

        assertThat(adapter.equipoClasificadoSegundaFase(UUID.randomUUID(), UUID.randomUUID())).isFalse();
    }

    @Test
    void equipoClasificadoSegundaFase_bracketVacio_retornaFalse() {
        UUID torneoId = UUID.randomUUID();
        stubByMatchAndBracket();
        when(responseSpec.body(TorneoResponse.class)).thenReturn(new TorneoResponse(torneoId));
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(List.of());

        assertThat(adapter.equipoClasificadoSegundaFase(UUID.randomUUID(), UUID.randomUUID())).isFalse();
    }

    @Test
    void equipoClasificadoSegundaFase_partidoNoExiste_retornaFalse() {
        stubByMatch();
        when(responseSpec.body(TorneoResponse.class)).thenThrow(notFound());

        assertThat(adapter.equipoClasificadoSegundaFase(UUID.randomUUID(), UUID.randomUUID())).isFalse();
    }

    @Test
    void equipoClasificadoSegundaFase_falloAlConsultarBracket_lanzaIntegrationException() {
        UUID torneoId = UUID.randomUUID();
        stubByMatchAndBracket();
        when(responseSpec.body(TorneoResponse.class)).thenReturn(new TorneoResponse(torneoId));
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenThrow(
                HttpClientErrorException.create(HttpStatus.SERVICE_UNAVAILABLE, "Unavailable",
                        HttpHeaders.EMPTY, new byte[0], null));

        assertThatThrownBy(() -> adapter.equipoClasificadoSegundaFase(UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(IntegrationException.class);
    }
}
