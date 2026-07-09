package co.edu.escuelaing.techcup.logistics.mapper;

import co.edu.escuelaing.techcup.logistics.dto.response.EntregaRefrigerioResponse;
import co.edu.escuelaing.techcup.logistics.entity.EntregaRefrigerio;

public final class EntregaRefrigerioMapper {

    private EntregaRefrigerioMapper() {
    }

    public static EntregaRefrigerioResponse toResponse(EntregaRefrigerio entity) {
        return new EntregaRefrigerioResponse(
                entity.getId(),
                entity.getDefinicionRefrigerioId(),
                entity.getPartidoId(),
                entity.getTipoDestinatario(),
                entity.getDestinatarioId(),
                entity.getResponsableId(),
                entity.getFechaEntrega(),
                entity.getObservaciones()
        );
    }
}
