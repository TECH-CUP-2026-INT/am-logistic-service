package co.edu.escuelaing.techcup.logistics.dto.request;

import java.util.UUID;

import co.edu.escuelaing.techcup.logistics.enums.TipoItemDotacion;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * La dotacion (peto, balon o kit) solo puede entregarse a un arbitro:
 * arbitroId se valida contra el Servicio de Equipos. cantidad indica cuantas
 * unidades fisicas registrar de una sola vez; cada una se guarda como un
 * item independiente (un documento por unidad), para poder rastrearla
 * individualmente en las etapas de entrega y devolucion.
 */
public record RegistrarItemDotacionRequest(

        @NotNull(message = "El arbitro destinatario es obligatorio")
        UUID arbitroId,

        @NotNull(message = "El tipo de item es obligatorio")
        TipoItemDotacion tipoItem,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        Integer cantidad,

        @NotNull(message = "El responsable asignado es obligatorio")
        UUID responsableAsignadoId,

        @Size(max = 500)
        String observaciones
) {
}
