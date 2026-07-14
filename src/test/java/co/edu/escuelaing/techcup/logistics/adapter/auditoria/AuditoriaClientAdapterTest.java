package co.edu.escuelaing.techcup.logistics.adapter.auditoria;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import co.edu.escuelaing.techcup.logistics.adapter.auditoria.dto.RegistroAuditoriaDTO;
import co.edu.escuelaing.techcup.logistics.adapter.auditoria.dto.TipoEntregaAuditoria;

class AuditoriaClientAdapterTest {

    private AuditoriaClientAdapter adapter;
    private RestClient restClient;
    private RestClient.RequestBodyUriSpec bodyUriSpec;
    private RestClient.ResponseSpec responseSpec;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        adapter = new AuditoriaClientAdapter("http://localhost:9999");
        restClient = mock(RestClient.class);
        bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);
        ReflectionTestUtils.setField(adapter, "restClient", restClient);
    }

    @SuppressWarnings("unchecked")
    @Test
    void reportarEntrega_exitoso_envíaPost() {
        RegistroAuditoriaDTO registro = new RegistroAuditoriaDTO(
                TipoEntregaAuditoria.REFRIGERIO, UUID.randomUUID(), "EQUIPO",
                UUID.randomUUID(), UUID.randomUUID(), Instant.now());

        when(restClient.post()).thenReturn(bodyUriSpec);
        when(bodyUriSpec.uri("/registros")).thenReturn(bodyUriSpec);
        when(bodyUriSpec.body(registro)).thenReturn(bodyUriSpec);
        when(bodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        adapter.reportarEntrega(registro);

        verify(responseSpec).toBodilessEntity();
    }

    @SuppressWarnings("unchecked")
    @Test
    void reportarEntrega_fallaDeRed_noPropagaExcepcion() {
        RegistroAuditoriaDTO registro = new RegistroAuditoriaDTO(
                TipoEntregaAuditoria.DOTACION, UUID.randomUUID(), "JUGADOR",
                UUID.randomUUID(), UUID.randomUUID(), Instant.now());

        when(restClient.post()).thenReturn(bodyUriSpec);
        when(bodyUriSpec.uri("/registros")).thenReturn(bodyUriSpec);
        when(bodyUriSpec.body(registro)).thenReturn(bodyUriSpec);
        when(bodyUriSpec.retrieve()).thenThrow(new RuntimeException("conexion rechazada"));

        adapter.reportarEntrega(registro);
    }
}
