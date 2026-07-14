package co.edu.escuelaing.techcup.logistics.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.escuelaing.techcup.logistics.dto.response.AuditEventResponse;
import co.edu.escuelaing.techcup.logistics.entity.DefinicionRefrigerio;
import co.edu.escuelaing.techcup.logistics.entity.EntregaRefrigerio;
import co.edu.escuelaing.techcup.logistics.entity.ItemDotacion;
import co.edu.escuelaing.techcup.logistics.enums.EstadoDotacion;
import co.edu.escuelaing.techcup.logistics.enums.TipoDestinatario;
import co.edu.escuelaing.techcup.logistics.enums.TipoEventoAuditoria;
import co.edu.escuelaing.techcup.logistics.enums.TipoItemDotacion;
import co.edu.escuelaing.techcup.logistics.repository.DefinicionRefrigerioRepository;
import co.edu.escuelaing.techcup.logistics.repository.EntregaRefrigerioRepository;
import co.edu.escuelaing.techcup.logistics.repository.ItemDotacionRepository;

@ExtendWith(MockitoExtension.class)
class AuditEventServiceImplTest {

    @Mock
    private DefinicionRefrigerioRepository definicionRefrigerioRepository;

    @Mock
    private EntregaRefrigerioRepository entregaRefrigerioRepository;

    @Mock
    private ItemDotacionRepository itemDotacionRepository;

    @InjectMocks
    private AuditEventServiceImpl service;

    private Instant base;

    @BeforeEach
    void setUp() {
        base = Instant.now();
    }

    @Test
    void listarEventos_agregaYOrdenaDescendentementePorTimestamp() {
        DefinicionRefrigerio definicion = DefinicionRefrigerio.builder()
                .id(UUID.randomUUID())
                .partidoId(UUID.randomUUID())
                .equipoId(UUID.randomUUID())
                .creadoPorId(UUID.randomUUID())
                .fechaCreacion(base.minus(3, ChronoUnit.HOURS))
                .build();

        EntregaRefrigerio entrega = EntregaRefrigerio.builder()
                .id(UUID.randomUUID())
                .definicionRefrigerioId(definicion.getId())
                .partidoId(definicion.getPartidoId())
                .tipoDestinatario(TipoDestinatario.EQUIPO)
                .destinatarioId(UUID.randomUUID())
                .responsableId(UUID.randomUUID())
                .fechaEntrega(base.minus(2, ChronoUnit.HOURS))
                .build();

        ItemDotacion itemDevuelto = ItemDotacion.builder()
                .id(UUID.randomUUID())
                .equipoId(UUID.randomUUID())
                .tipoItem(TipoItemDotacion.PETO)
                .cantidad(1)
                .estado(EstadoDotacion.DEVUELTO)
                .responsableAsignadoId(UUID.randomUUID())
                .entregadoPorId(UUID.randomUUID())
                .recibidoPorId(UUID.randomUUID())
                .fechaRegistro(base.minus(1, ChronoUnit.HOURS))
                .fechaEntrega(base.minus(30, ChronoUnit.MINUTES))
                .fechaDevolucion(base)
                .build();

        when(definicionRefrigerioRepository.findAll()).thenReturn(List.of(definicion));
        when(entregaRefrigerioRepository.findAll()).thenReturn(List.of(entrega));
        when(itemDotacionRepository.findAll()).thenReturn(List.of(itemDevuelto));

        List<AuditEventResponse> eventos = service.listarEventos();

        // 1 definicion + 1 entrega + 3 eventos de dotacion (registrada, entregada, devuelta)
        assertThat(eventos).hasSize(5);
        assertThat(eventos.get(0).tipo()).isEqualTo(TipoEventoAuditoria.DOTACION_DEVUELTA);
        assertThat(eventos.get(0).timestamp()).isEqualTo(base);
        assertThat(eventos).isSortedAccordingTo(
                (a, b) -> b.timestamp().compareTo(a.timestamp()));
    }

    @Test
    void listarEventos_itemPendiente_soloGeneraEventoDeRegistro() {
        ItemDotacion pendiente = ItemDotacion.builder()
                .id(UUID.randomUUID())
                .equipoId(UUID.randomUUID())
                .tipoItem(TipoItemDotacion.BALON)
                .cantidad(2)
                .estado(EstadoDotacion.PENDIENTE)
                .responsableAsignadoId(UUID.randomUUID())
                .fechaRegistro(base)
                .build();

        when(definicionRefrigerioRepository.findAll()).thenReturn(List.of());
        when(entregaRefrigerioRepository.findAll()).thenReturn(List.of());
        when(itemDotacionRepository.findAll()).thenReturn(List.of(pendiente));

        List<AuditEventResponse> eventos = service.listarEventos();

        assertThat(eventos).hasSize(1);
        assertThat(eventos.get(0).tipo()).isEqualTo(TipoEventoAuditoria.DOTACION_REGISTRADA);
    }

    @Test
    void listarEventos_sinDatos_retornaListaVacia() {
        when(definicionRefrigerioRepository.findAll()).thenReturn(List.of());
        when(entregaRefrigerioRepository.findAll()).thenReturn(List.of());
        when(itemDotacionRepository.findAll()).thenReturn(List.of());

        assertThat(service.listarEventos()).isEmpty();
    }
}
