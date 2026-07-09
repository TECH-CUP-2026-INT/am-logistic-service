package co.edu.escuelaing.techcup.logistics.adapter.auditoria;

import co.edu.escuelaing.techcup.logistics.adapter.auditoria.dto.RegistroAuditoriaDTO;

/**
 * Puerto hacia el Servicio de Auditoria: reporta cada entrega registrada
 * (fecha, responsable, destinatario) con fines de trazabilidad.
 *
 * Esta operacion es "fire-and-forget": un fallo al reportar NO debe impedir
 * que la entrega quede registrada en Logistica.
 */
public interface AuditoriaClientPort {

    void reportarEntrega(RegistroAuditoriaDTO registro);
}
