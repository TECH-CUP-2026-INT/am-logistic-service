package co.edu.escuelaing.techcup.logistics.adapter.auditoria.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Payload de trazabilidad reportado a Auditoria por cada entrega registrada.
 * TODO: confirmar con el equipo de Auditoria la forma real del contrato esperado.
 */
public record RegistroAuditoriaDTO(
        TipoEntregaAuditoria tipoEntrega,
        UUID referenciaId,
        String destinatarioTipo,
        UUID destinatarioId,
        UUID responsableId,
        Instant fecha
) {
}
