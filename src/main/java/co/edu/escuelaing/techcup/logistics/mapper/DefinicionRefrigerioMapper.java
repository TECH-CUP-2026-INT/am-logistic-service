package co.edu.escuelaing.techcup.logistics.mapper;

import java.util.List;

import co.edu.escuelaing.techcup.logistics.dto.response.DefinicionRefrigerioResponse;
import co.edu.escuelaing.techcup.logistics.dto.response.ItemRefrigerioResponse;
import co.edu.escuelaing.techcup.logistics.entity.DefinicionRefrigerio;
import co.edu.escuelaing.techcup.logistics.entity.ItemRefrigerioDefinido;

public final class DefinicionRefrigerioMapper {

    private DefinicionRefrigerioMapper() {
    }

    public static DefinicionRefrigerioResponse toResponse(DefinicionRefrigerio entity) {
        List<ItemRefrigerioResponse> items = entity.getItems().stream()
                .map(DefinicionRefrigerioMapper::toItemResponse)
                .toList();

        return new DefinicionRefrigerioResponse(
                entity.getId(),
                entity.getPartidoId(),
                entity.getEquipoId(),
                items,
                entity.getObservaciones(),
                entity.getCreadoPorId(),
                entity.getFechaCreacion()
        );
    }

    private static ItemRefrigerioResponse toItemResponse(ItemRefrigerioDefinido item) {
        return new ItemRefrigerioResponse(item.getDescripcion(), item.getCantidad());
    }
}
