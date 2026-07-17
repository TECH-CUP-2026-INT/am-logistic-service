package co.edu.escuelaing.techcup.logistics.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * El partidoId y el responsable no viajan en el body: el partido se deriva de
 * la definicion de refrigerio referenciada, y el responsable es el organizador
 * autenticado (header X-User-Id). El refrigerio solo puede entregarse al
 * Capitan del equipo clasificado: capitanId se valida contra el Servicio de
 * Equipos antes de registrar la entrega.
 */
public record RegistrarEntregaRefrigerioRequest(

        @NotNull(message = "La definicion de refrigerio es obligatoria")
        UUID definicionRefrigerioId,

        @NotNull(message = "El capitan destinatario es obligatorio")
        UUID capitanId,

        @Size(max = 500)
        String observaciones
) {
}
