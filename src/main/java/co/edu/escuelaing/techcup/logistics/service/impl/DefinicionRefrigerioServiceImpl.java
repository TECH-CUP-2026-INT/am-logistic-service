package co.edu.escuelaing.techcup.logistics.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.escuelaing.techcup.logistics.adapter.equipos.EquipoClientPort;
import co.edu.escuelaing.techcup.logistics.adapter.torneos.TorneoClientPort;
import co.edu.escuelaing.techcup.logistics.dto.request.CrearDefinicionRefrigerioRequest;
import co.edu.escuelaing.techcup.logistics.dto.response.DefinicionRefrigerioResponse;
import co.edu.escuelaing.techcup.logistics.entity.DefinicionRefrigerio;
import co.edu.escuelaing.techcup.logistics.entity.ItemRefrigerioDefinido;
import co.edu.escuelaing.techcup.logistics.exception.DuplicateResourceException;
import co.edu.escuelaing.techcup.logistics.exception.EquipoNoClasificadoException;
import co.edu.escuelaing.techcup.logistics.exception.RecursoNoEncontradoException;
import co.edu.escuelaing.techcup.logistics.mapper.DefinicionRefrigerioMapper;
import co.edu.escuelaing.techcup.logistics.repository.DefinicionRefrigerioRepository;
import co.edu.escuelaing.techcup.logistics.service.DefinicionRefrigerioService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefinicionRefrigerioServiceImpl implements DefinicionRefrigerioService {

    private final DefinicionRefrigerioRepository repository;
    private final TorneoClientPort torneoClientPort;
    private final EquipoClientPort equipoClientPort;

    @Override
    @Transactional
    public DefinicionRefrigerioResponse crear(CrearDefinicionRefrigerioRequest request, UUID creadoPorId) {
        if (!torneoClientPort.existePartido(request.partidoId())) {
            throw new RecursoNoEncontradoException(
                    "El partido " + request.partidoId() + " no existe en el Servicio de Torneos");
        }
        if (!equipoClientPort.existeEquipo(request.equipoId())) {
            throw new RecursoNoEncontradoException(
                    "El equipo " + request.equipoId() + " no existe en el Servicio de Equipos");
        }
        if (!torneoClientPort.equipoClasificadoSegundaFase(request.partidoId(), request.equipoId())) {
            throw new EquipoNoClasificadoException(
                    "El equipo " + request.equipoId() + " aun no ha clasificado a segunda fase "
                            + "segun los resultados registrados en el Servicio de Torneos; "
                            + "no se puede definir un refrigerio para el");
        }
        if (repository.existsByPartidoIdAndEquipoId(request.partidoId(), request.equipoId())) {
            throw new DuplicateResourceException(
                    "Ya existe una definicion de refrigerio para el equipo " + request.equipoId()
                            + " en el partido " + request.partidoId());
        }

        List<ItemRefrigerioDefinido> items = new ArrayList<>();
        for (var item : request.items()) {
            items.add(ItemRefrigerioDefinido.builder()
                    .descripcion(item.descripcion())
                    .cantidad(item.cantidad())
                    .build());
        }

        DefinicionRefrigerio definicion = DefinicionRefrigerio.builder()
                .partidoId(request.partidoId())
                .equipoId(request.equipoId())
                .items(items)
                .observaciones(request.observaciones())
                .creadoPorId(creadoPorId)
                .fechaCreacion(Instant.now())
                .build();

        return DefinicionRefrigerioMapper.toResponse(repository.save(definicion));
    }

    @Override
    public List<DefinicionRefrigerioResponse> listarPorPartido(UUID partidoId) {
        return repository.findByPartidoId(partidoId).stream()
                .map(DefinicionRefrigerioMapper::toResponse)
                .toList();
    }
}
