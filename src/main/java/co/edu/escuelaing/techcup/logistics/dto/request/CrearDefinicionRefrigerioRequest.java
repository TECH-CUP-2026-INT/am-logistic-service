package co.edu.escuelaing.techcup.logistics.dto.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearDefinicionRefrigerioRequest(

        @NotNull(message = "El partido es obligatorio")
        UUID partidoId,

        @NotNull(message = "El equipo es obligatorio")
        UUID equipoId,

        @NotEmpty(message = "Debe definir al menos un refrigerio")
        @Valid
        List<ItemRefrigerioRequest> items,

        @Size(max = 500)
        String observaciones
) {
}
