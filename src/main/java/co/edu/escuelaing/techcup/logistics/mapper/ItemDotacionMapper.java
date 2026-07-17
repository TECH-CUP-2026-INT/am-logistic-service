package co.edu.escuelaing.techcup.logistics.mapper;

import co.edu.escuelaing.techcup.logistics.dto.response.ItemDotacionResponse;
import co.edu.escuelaing.techcup.logistics.entity.ItemDotacion;

public final class ItemDotacionMapper {

    private ItemDotacionMapper() {
    }

    public static ItemDotacionResponse toResponse(ItemDotacion entity) {
        return new ItemDotacionResponse(
                entity.getId(),
                entity.getArbitroId(),
                entity.getTipoItem(),
                entity.getEstado(),
                entity.getResponsableAsignadoId(),
                entity.getEntregadoPorId(),
                entity.getFechaRegistro(),
                entity.getFechaEntrega(),
                entity.getRecibidoPorId(),
                entity.getFechaDevolucion(),
                entity.getObservaciones()
        );
    }
}
