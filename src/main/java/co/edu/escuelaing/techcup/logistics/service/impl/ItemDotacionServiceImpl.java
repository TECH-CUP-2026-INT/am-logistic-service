package co.edu.escuelaing.techcup.logistics.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.escuelaing.techcup.logistics.adapter.auditoria.AuditoriaClientPort;
import co.edu.escuelaing.techcup.logistics.adapter.auditoria.dto.RegistroAuditoriaDTO;
import co.edu.escuelaing.techcup.logistics.adapter.auditoria.dto.TipoEntregaAuditoria;
import co.edu.escuelaing.techcup.logistics.adapter.equipos.EquipoClientPort;
import co.edu.escuelaing.techcup.logistics.dto.request.MarcarDotacionEntregadaRequest;
import co.edu.escuelaing.techcup.logistics.dto.request.RegistrarDevolucionDotacionRequest;
import co.edu.escuelaing.techcup.logistics.dto.request.RegistrarItemDotacionRequest;
import co.edu.escuelaing.techcup.logistics.dto.response.ItemDotacionResponse;
import co.edu.escuelaing.techcup.logistics.entity.ItemDotacion;
import co.edu.escuelaing.techcup.logistics.enums.EstadoDotacion;
import co.edu.escuelaing.techcup.logistics.exception.DuplicateResourceException;
import co.edu.escuelaing.techcup.logistics.exception.RecursoNoEncontradoException;
import co.edu.escuelaing.techcup.logistics.mapper.ItemDotacionMapper;
import co.edu.escuelaing.techcup.logistics.repository.ItemDotacionRepository;
import co.edu.escuelaing.techcup.logistics.service.ItemDotacionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemDotacionServiceImpl implements ItemDotacionService {

    private final ItemDotacionRepository repository;
    private final EquipoClientPort equipoClientPort;
    private final AuditoriaClientPort auditoriaClientPort;

    @Override
    @Transactional
    public List<ItemDotacionResponse> registrar(RegistrarItemDotacionRequest request) {
        if (!equipoClientPort.existeArbitro(request.arbitroId())) {
            throw new RecursoNoEncontradoException(
                    "El arbitro " + request.arbitroId() + " no existe en el Servicio de Equipos");
        }

        Instant fechaRegistro = Instant.now();
        List<ItemDotacion> items = new ArrayList<>();
        for (int i = 0; i < request.cantidad(); i++) {
            items.add(ItemDotacion.builder()
                    .arbitroId(request.arbitroId())
                    .tipoItem(request.tipoItem())
                    .estado(EstadoDotacion.PENDIENTE)
                    .responsableAsignadoId(request.responsableAsignadoId())
                    .fechaRegistro(fechaRegistro)
                    .observaciones(request.observaciones())
                    .build());
        }

        return repository.saveAll(items).stream()
                .map(ItemDotacionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ItemDotacionResponse marcarEntregado(UUID itemId, MarcarDotacionEntregadaRequest request, UUID entregadoPorId) {
        ItemDotacion item = repository.findById(itemId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El item de dotacion " + itemId + " no existe"));

        if (item.getEstado() == EstadoDotacion.ENTREGADO) {
            throw new DuplicateResourceException(
                    "El item de dotacion " + itemId + " ya fue registrado como entregado");
        }

        item.setEstado(EstadoDotacion.ENTREGADO);
        item.setEntregadoPorId(entregadoPorId);
        item.setFechaEntrega(Instant.now());
        if (request.observaciones() != null) {
            item.setObservaciones(request.observaciones());
        }

        ItemDotacion actualizado = repository.save(item);

        auditoriaClientPort.reportarEntrega(new RegistroAuditoriaDTO(
                TipoEntregaAuditoria.DOTACION,
                actualizado.getId(),
                "ARBITRO",
                actualizado.getArbitroId(),
                actualizado.getEntregadoPorId(),
                actualizado.getFechaEntrega()
        ));

        return ItemDotacionMapper.toResponse(actualizado);
    }

    @Override
    @Transactional
    public ItemDotacionResponse registrarDevolucion(
            UUID itemId, RegistrarDevolucionDotacionRequest request, UUID recibidoPorId) {
        ItemDotacion item = repository.findById(itemId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El item de dotacion " + itemId + " no existe"));

        if (item.getEstado() != EstadoDotacion.ENTREGADO) {
            throw new DuplicateResourceException(
                    "El item de dotacion " + itemId + " no se puede devolver porque su estado actual es "
                            + item.getEstado());
        }

        item.setEstado(EstadoDotacion.DEVUELTO);
        item.setRecibidoPorId(recibidoPorId);
        item.setFechaDevolucion(Instant.now());
        if (request.observaciones() != null) {
            item.setObservaciones(request.observaciones());
        }

        ItemDotacion actualizado = repository.save(item);

        auditoriaClientPort.reportarEntrega(new RegistroAuditoriaDTO(
                TipoEntregaAuditoria.DOTACION,
                actualizado.getId(),
                "ARBITRO",
                actualizado.getArbitroId(),
                actualizado.getRecibidoPorId(),
                actualizado.getFechaDevolucion()
        ));

        return ItemDotacionMapper.toResponse(actualizado);
    }

    @Override
    public List<ItemDotacionResponse> listarPorArbitro(UUID arbitroId, EstadoDotacion estado) {
        List<ItemDotacion> items = estado == null
                ? repository.findByArbitroId(arbitroId)
                : repository.findByArbitroIdAndEstado(arbitroId, estado);
        return items.stream().map(ItemDotacionMapper::toResponse).toList();
    }
}
