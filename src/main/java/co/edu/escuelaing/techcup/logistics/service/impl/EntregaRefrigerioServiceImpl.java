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
import co.edu.escuelaing.techcup.logistics.dto.request.RegistrarEntregaRefrigerioRequest;
import co.edu.escuelaing.techcup.logistics.dto.response.EntregaRefrigerioResponse;
import co.edu.escuelaing.techcup.logistics.entity.DefinicionRefrigerio;
import co.edu.escuelaing.techcup.logistics.entity.EntregaRefrigerio;
import co.edu.escuelaing.techcup.logistics.enums.TipoDestinatario;
import co.edu.escuelaing.techcup.logistics.exception.DuplicateResourceException;
import co.edu.escuelaing.techcup.logistics.exception.RecursoNoEncontradoException;
import co.edu.escuelaing.techcup.logistics.mapper.EntregaRefrigerioMapper;
import co.edu.escuelaing.techcup.logistics.repository.DefinicionRefrigerioRepository;
import co.edu.escuelaing.techcup.logistics.repository.EntregaRefrigerioRepository;
import co.edu.escuelaing.techcup.logistics.service.EntregaRefrigerioService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EntregaRefrigerioServiceImpl implements EntregaRefrigerioService {

    private final EntregaRefrigerioRepository repository;
    private final DefinicionRefrigerioRepository definicionRepository;
    private final EquipoClientPort equipoClientPort;
    private final AuditoriaClientPort auditoriaClientPort;

    @Override
    @Transactional
    public EntregaRefrigerioResponse registrar(RegistrarEntregaRefrigerioRequest request, UUID responsableId) {
        DefinicionRefrigerio definicion = definicionRepository.findById(request.definicionRefrigerioId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La definicion de refrigerio " + request.definicionRefrigerioId() + " no existe"));

        validarDestinatario(request.tipoDestinatario(), request.destinatarioId(), definicion.getEquipoId());

        if (repository.existsByPartidoIdAndTipoDestinatarioAndDestinatarioId(
                definicion.getPartidoId(), request.tipoDestinatario(), request.destinatarioId())) {
            throw new DuplicateResourceException(
                    "Ya se registro una entrega de refrigerio para el destinatario " + request.destinatarioId()
                            + " en el partido " + definicion.getPartidoId());
        }

        EntregaRefrigerio entrega = EntregaRefrigerio.builder()
                .definicionRefrigerioId(definicion.getId())
                .partidoId(definicion.getPartidoId())
                .tipoDestinatario(request.tipoDestinatario())
                .destinatarioId(request.destinatarioId())
                .responsableId(responsableId)
                .fechaEntrega(Instant.now())
                .observaciones(request.observaciones())
                .build();

        EntregaRefrigerio guardada = repository.save(entrega);

        auditoriaClientPort.reportarEntrega(new RegistroAuditoriaDTO(
                TipoEntregaAuditoria.REFRIGERIO,
                guardada.getId(),
                guardada.getTipoDestinatario().name(),
                guardada.getDestinatarioId(),
                guardada.getResponsableId(),
                guardada.getFechaEntrega()
        ));

        return EntregaRefrigerioMapper.toResponse(guardada);
    }

    @Override
    public List<EntregaRefrigerioResponse> listarPorPartido(UUID partidoId) {
        return repository.findByPartidoId(partidoId).stream()
                .map(EntregaRefrigerioMapper::toResponse)
                .toList();
    }

    private void validarDestinatario(TipoDestinatario tipo, UUID destinatarioId, UUID equipoIdDefinicion) {
        boolean valido = switch (tipo) {
            case EQUIPO -> destinatarioId.equals(equipoIdDefinicion) && equipoClientPort.existeEquipo(destinatarioId);
            case JUGADOR -> equipoClientPort.existeJugadorEnEquipo(destinatarioId, equipoIdDefinicion);
        };
        if (!valido) {
            throw new RecursoNoEncontradoException(
                    "El destinatario " + destinatarioId + " no es valido para el equipo " + equipoIdDefinicion);
        }
    }
}
