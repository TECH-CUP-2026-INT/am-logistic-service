package co.edu.escuelaing.techcup.logistics.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import co.edu.escuelaing.techcup.logistics.dto.response.AuditEventResponse;
import co.edu.escuelaing.techcup.logistics.entity.DefinicionRefrigerio;
import co.edu.escuelaing.techcup.logistics.entity.EntregaRefrigerio;
import co.edu.escuelaing.techcup.logistics.entity.ItemDotacion;
import co.edu.escuelaing.techcup.logistics.enums.EstadoDotacion;
import co.edu.escuelaing.techcup.logistics.enums.TipoEventoAuditoria;
import co.edu.escuelaing.techcup.logistics.repository.DefinicionRefrigerioRepository;
import co.edu.escuelaing.techcup.logistics.repository.EntregaRefrigerioRepository;
import co.edu.escuelaing.techcup.logistics.repository.ItemDotacionRepository;
import co.edu.escuelaing.techcup.logistics.service.AuditEventService;
import lombok.RequiredArgsConstructor;

/**
 * RF-04: agrega, en memoria, los eventos de dominio que ya ocurrieron sobre
 * las 3 entidades propias de Logistica (sin tabla de auditoria dedicada,
 * reutilizando los timestamps/actor ya persistidos en cada una), y los
 * expone ordenados descendentemente por fecha.
 */
@Service
@RequiredArgsConstructor
public class AuditEventServiceImpl implements AuditEventService {

    private static final String PARA_ARBITRO = " para arbitro ";

    private final DefinicionRefrigerioRepository definicionRefrigerioRepository;
    private final EntregaRefrigerioRepository entregaRefrigerioRepository;
    private final ItemDotacionRepository itemDotacionRepository;

    @Override
    public List<AuditEventResponse> listarEventos() {
        List<AuditEventResponse> eventos = new ArrayList<>();

        for (DefinicionRefrigerio definicion : definicionRefrigerioRepository.findAll()) {
            eventos.add(new AuditEventResponse(
                    TipoEventoAuditoria.DEFINICION_REFRIGERIO_CREADA,
                    definicion.getId(),
                    definicion.getCreadoPorId(),
                    definicion.getFechaCreacion(),
                    "Definicion de refrigerio para equipo " + definicion.getEquipoId()
                            + " en partido " + definicion.getPartidoId()));
        }

        for (EntregaRefrigerio entrega : entregaRefrigerioRepository.findAll()) {
            eventos.add(new AuditEventResponse(
                    TipoEventoAuditoria.ENTREGA_REFRIGERIO_REGISTRADA,
                    entrega.getId(),
                    entrega.getResponsableId(),
                    entrega.getFechaEntrega(),
                    "Entrega de refrigerio al capitan " + entrega.getCapitanId()
                            + " del equipo " + entrega.getEquipoId()));
        }

        for (ItemDotacion item : itemDotacionRepository.findAll()) {
            eventos.add(new AuditEventResponse(
                    TipoEventoAuditoria.DOTACION_REGISTRADA,
                    item.getId(),
                    item.getResponsableAsignadoId(),
                    item.getFechaRegistro(),
                    "Registro de " + item.getTipoItem() + PARA_ARBITRO + item.getArbitroId()));

            if (item.getEstado() == EstadoDotacion.ENTREGADO || item.getEstado() == EstadoDotacion.DEVUELTO) {
                eventos.add(new AuditEventResponse(
                        TipoEventoAuditoria.DOTACION_ENTREGADA,
                        item.getId(),
                        item.getEntregadoPorId(),
                        item.getFechaEntrega(),
                        "Entrega de " + item.getTipoItem() + PARA_ARBITRO + item.getArbitroId()));
            }

            if (item.getEstado() == EstadoDotacion.DEVUELTO) {
                eventos.add(new AuditEventResponse(
                        TipoEventoAuditoria.DOTACION_DEVUELTA,
                        item.getId(),
                        item.getRecibidoPorId(),
                        item.getFechaDevolucion(),
                        "Devolucion de " + item.getTipoItem() + PARA_ARBITRO + item.getArbitroId()));
            }
        }

        eventos.sort(Comparator.comparing(AuditEventResponse::timestamp,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return eventos;
    }
}
