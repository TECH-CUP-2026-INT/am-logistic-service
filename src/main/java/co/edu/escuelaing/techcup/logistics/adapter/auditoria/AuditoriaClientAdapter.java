package co.edu.escuelaing.techcup.logistics.adapter.auditoria;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import co.edu.escuelaing.techcup.logistics.adapter.auditoria.dto.RegistroAuditoriaDTO;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador REST hacia el Servicio de Auditoria.
 *
 * Nota: contrato pendiente de confirmar con el equipo de Auditoria:
 *   - Ruta real para reportar una entrega (se asume POST /registros).
 *   - Forma exacta del payload esperado.
 *   - Cuando Auditoria defina un mecanismo de eventos/cola, este adaptador
 *     deberia reemplazarse por un publisher asincrono en vez de HTTP.
 *
 * Se ejecuta en un hilo separado (@Async) y nunca propaga excepciones: reportar
 * a Auditoria es best-effort y no debe romper el flujo principal de Logistica.
 */
@Component
@Slf4j
public class AuditoriaClientAdapter implements AuditoriaClientPort {

    private final RestClient restClient;

    public AuditoriaClientAdapter(@Value("${logistics.integration.auditoria.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    @Async
    public void reportarEntrega(RegistroAuditoriaDTO registro) {
        try {
            restClient.post()
                    .uri("/registros")
                    .body(registro)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("No se pudo reportar la entrega {} a Auditoria: {}", registro.referenciaId(), e.getMessage());
        }
    }
}
