package co.edu.escuelaing.techcup.logistics.mapper;

import co.edu.escuelaing.techcup.logistics.dto.response.ItemDotacionResponse;
import co.edu.escuelaing.techcup.logistics.entity.ItemDotacion;

public final class ItemDotacionMapper {

    private ItemDotacionMapper() {
    }

    public static ItemDotacionResponse toResponse(ItemDotacion entity) {
        return new ItemDotacionResponse(
                entity.getId(),
                entity.getEquipoId(),
                entity.getTipoItem(),
                entity.getCantidad(),
                entity.getEstado(),
                entity.getResponsableAsignadoId(),
                entity.getEntregadoPorId(),
                entity.getFechaRegistro(),
                entity.getFechaEntrega(),
                entity.getObservaciones()
        );
    }
}
