package co.edu.escuelaing.techcup.logistics.service;

import java.util.List;
import java.util.UUID;

import co.edu.escuelaing.techcup.logistics.dto.request.CrearDefinicionRefrigerioRequest;
import co.edu.escuelaing.techcup.logistics.dto.response.DefinicionRefrigerioResponse;

public interface DefinicionRefrigerioService {

    DefinicionRefrigerioResponse crear(CrearDefinicionRefrigerioRequest request, UUID creadoPorId);

    List<DefinicionRefrigerioResponse> listarPorPartido(UUID partidoId);
}
