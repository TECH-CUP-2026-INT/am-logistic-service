package co.edu.escuelaing.techcup.logistics.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemRefrigerioRequest(

        @NotBlank(message = "La descripcion del refrigerio es obligatoria")
        String descripcion,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        Integer cantidad
) {
}
