package co.edu.escuelaing.techcup.logistics.service;

import java.util.List;
import java.util.UUID;

import co.edu.escuelaing.techcup.logistics.dto.request.MarcarDotacionEntregadaRequest;
import co.edu.escuelaing.techcup.logistics.dto.request.RegistrarItemDotacionRequest;
import co.edu.escuelaing.techcup.logistics.dto.response.ItemDotacionResponse;
import co.edu.escuelaing.techcup.logistics.enums.EstadoDotacion;

public interface ItemDotacionService {

    ItemDotacionResponse registrar(RegistrarItemDotacionRequest request);

    ItemDotacionResponse marcarEntregado(UUID itemId, MarcarDotacionEntregadaRequest request, UUID entregadoPorId);

    List<ItemDotacionResponse> listarPorEquipo(UUID equipoId, EstadoDotacion estado);
}
