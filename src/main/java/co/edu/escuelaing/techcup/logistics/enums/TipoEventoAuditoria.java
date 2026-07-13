package co.edu.escuelaing.techcup.logistics.enums;

/**
 * Tipos de evento del feed de auditoria local (RF-04): agregacion de sucesos
 * que ya ocurrieron sobre las 3 entidades propias de Logistica.
 */
public enum TipoEventoAuditoria {
    DEFINICION_REFRIGERIO_CREADA,
    ENTREGA_REFRIGERIO_REGISTRADA,
    DOTACION_REGISTRADA,
    DOTACION_ENTREGADA,
    DOTACION_DEVUELTA
}
