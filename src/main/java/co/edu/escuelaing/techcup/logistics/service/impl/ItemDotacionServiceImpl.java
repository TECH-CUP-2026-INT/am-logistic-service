package co.edu.escuelaing.techcup.logistics.service.impl;

import java.time.Instant;
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
    public ItemDotacionResponse registrar(RegistrarItemDotacionRequest request) {
        if (!equipoClientPort.existeEquipo(request.equipoId())) {
            throw new RecursoNoEncontradoException(
                    "El equipo " + request.equipoId() + " no existe en el Servicio de Equipos");
        }

        ItemDotacion item = ItemDotacion.builder()
                .equipoId(request.equipoId())
                .tipoItem(request.tipoItem())
                .cantidad(request.cantidad())
                .estado(EstadoDotacion.PENDIENTE)
                .responsableAsignadoId(request.responsableAsignadoId())
                .fechaRegistro(Instant.now())
                .observaciones(request.observaciones())
                .build();

        return ItemDotacionMapper.toResponse(repository.save(item));
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
                "EQUIPO",
                actualizado.getEquipoId(),
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
                "EQUIPO",
                actualizado.getEquipoId(),
                actualizado.getRecibidoPorId(),
                actualizado.getFechaDevolucion()
        ));

        return ItemDotacionMapper.toResponse(actualizado);
    }

    @Override
    public List<ItemDotacionResponse> listarPorEquipo(UUID equipoId, EstadoDotacion estado) {
        List<ItemDotacion> items = estado == null
                ? repository.findByEquipoId(equipoId)
                : repository.findByEquipoIdAndEstado(equipoId, estado);
        return items.stream().map(ItemDotacionMapper::toResponse).toList();
    }
}
