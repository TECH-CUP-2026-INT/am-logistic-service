package co.edu.escuelaing.techcup.logistics.dto.request;

import java.util.UUID;

import co.edu.escuelaing.techcup.logistics.enums.TipoItemDotacion;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarItemDotacionRequest(

        @NotNull(message = "El equipo destinatario es obligatorio")
        UUID equipoId,

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
