package co.edu.escuelaing.techcup.logistics.dto.response;

import java.time.Instant;
import java.util.UUID;

import co.edu.escuelaing.techcup.logistics.enums.TipoEventoAuditoria;

/**
 * Evento del feed de auditoria local (RF-04): union en memoria de sucesos que
 * ya ocurrieron sobre DefinicionRefrigerio, EntregaRefrigerio e ItemDotacion,
 * ordenados descendentemente por timestamp.
 */
public record AuditEventResponse(
        TipoEventoAuditoria tipo,
        UUID entidadId,
        UUID actorId,
        Instant timestamp,
        String detalle
) {
}
