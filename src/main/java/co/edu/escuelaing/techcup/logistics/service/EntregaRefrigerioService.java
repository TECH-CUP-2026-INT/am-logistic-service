package co.edu.escuelaing.techcup.logistics.service;

import java.util.List;
import java.util.UUID;

import co.edu.escuelaing.techcup.logistics.dto.request.RegistrarEntregaRefrigerioRequest;
import co.edu.escuelaing.techcup.logistics.dto.response.EntregaRefrigerioResponse;

public interface EntregaRefrigerioService {

    EntregaRefrigerioResponse registrar(RegistrarEntregaRefrigerioRequest request, UUID responsableId);

    List<EntregaRefrigerioResponse> listarPorPartido(UUID partidoId);
}
