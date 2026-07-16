package co.edu.escuelaing.techcup.logistics.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.escuelaing.techcup.logistics.adapter.auditoria.AuditoriaClientPort;
import co.edu.escuelaing.techcup.logistics.adapter.auditoria.dto.RegistroAuditoriaDTO;
import co.edu.escuelaing.techcup.logistics.adapter.equipos.EquipoClientPort;
import co.edu.escuelaing.techcup.logistics.dto.request.MarcarDotacionEntregadaRequest;
import co.edu.escuelaing.techcup.logistics.dto.request.RegistrarDevolucionDotacionRequest;
import co.edu.escuelaing.techcup.logistics.dto.request.RegistrarItemDotacionRequest;
import co.edu.escuelaing.techcup.logistics.dto.response.ItemDotacionResponse;
import co.edu.escuelaing.techcup.logistics.entity.ItemDotacion;
import co.edu.escuelaing.techcup.logistics.enums.EstadoDotacion;
import co.edu.escuelaing.techcup.logistics.enums.TipoItemDotacion;
import co.edu.escuelaing.techcup.logistics.exception.DuplicateResourceException;
import co.edu.escuelaing.techcup.logistics.exception.RecursoNoEncontradoException;
import co.edu.escuelaing.techcup.logistics.repository.ItemDotacionRepository;

@ExtendWith(MockitoExtension.class)
class ItemDotacionServiceImplTest {

    @Mock
    private ItemDotacionRepository repository;

    @Mock
    private EquipoClientPort equipoClientPort;

    @Mock
    private AuditoriaClientPort auditoriaClientPort;

    @InjectMocks
    private ItemDotacionServiceImpl service;

    private UUID equipoId;
    private UUID responsableId;

    @BeforeEach
    void setUp() {
        equipoId = UUID.randomUUID();
        responsableId = UUID.randomUUID();
    }

    @Test
    void registrar_equipoValido_guardaItemComoPendiente() {
        RegistrarItemDotacionRequest request = new RegistrarItemDotacionRequest(
                equipoId, TipoItemDotacion.PETO, 20, responsableId, null);

        when(equipoClientPort.existeEquipo(equipoId)).thenReturn(true);
        when(repository.save(any(ItemDotacion.class))).thenAnswer(inv -> {
            ItemDotacion entity = inv.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ItemDotacionResponse response = service.registrar(request);

        assertThat(response.estado()).isEqualTo(EstadoDotacion.PENDIENTE);
        assertThat(response.equipoId()).isEqualTo(equipoId);
        assertThat(response.cantidad()).isEqualTo(20);
    }

    @Test
    void registrar_equipoInexistente_lanzaRecursoNoEncontrado() {
        RegistrarItemDotacionRequest request = new RegistrarItemDotacionRequest(
                equipoId, TipoItemDotacion.BALON, 5, responsableId, null);
        when(equipoClientPort.existeEquipo(equipoId)).thenReturn(false);

        assertThatThrownBy(() -> service.registrar(request))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void marcarEntregado_itemPendiente_loMarcaEntregadoYReportaAuditoria() {
        UUID itemId = UUID.randomUUID();
        ItemDotacion pendiente = ItemDotacion.builder()
                .id(itemId)
                .equipoId(equipoId)
                .tipoItem(TipoItemDotacion.KIT)
                .cantidad(1)
                .estado(EstadoDotacion.PENDIENTE)
                .responsableAsignadoId(responsableId)
                .fechaRegistro(Instant.now())
                .build();

        when(repository.findById(itemId)).thenReturn(Optional.of(pendiente));
        when(repository.save(any(ItemDotacion.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemDotacionResponse response = service.marcarEntregado(
                itemId, new MarcarDotacionEntregadaRequest("Entregado en vestier"), responsableId);

        assertThat(response.estado()).isEqualTo(EstadoDotacion.ENTREGADO);
        assertThat(response.entregadoPorId()).isEqualTo(responsableId);
        assertThat(response.fechaEntrega()).isNotNull();
        verify(auditoriaClientPort, times(1)).reportarEntrega(any(RegistroAuditoriaDTO.class));
    }

    @Test
    void marcarEntregado_itemYaEntregado_lanzaDuplicateResource() {
        UUID itemId = UUID.randomUUID();
        ItemDotacion yaEntregado = ItemDotacion.builder()
                .id(itemId)
                .equipoId(equipoId)
                .tipoItem(TipoItemDotacion.KIT)
                .cantidad(1)
                .estado(EstadoDotacion.ENTREGADO)
                .responsableAsignadoId(responsableId)
                .entregadoPorId(responsableId)
                .fechaRegistro(Instant.now())
                .fechaEntrega(Instant.now())
                .build();

        when(repository.findById(itemId)).thenReturn(Optional.of(yaEntregado));
        MarcarDotacionEntregadaRequest request = new MarcarDotacionEntregadaRequest(null);

        assertThatThrownBy(() -> service.marcarEntregado(itemId, request, responsableId))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
        verify(auditoriaClientPort, never()).reportarEntrega(any());
    }

    @Test
    void marcarEntregado_itemInexistente_lanzaRecursoNoEncontrado() {
        UUID itemId = UUID.randomUUID();
        when(repository.findById(itemId)).thenReturn(Optional.empty());
        MarcarDotacionEntregadaRequest request = new MarcarDotacionEntregadaRequest(null);

        assertThatThrownBy(() -> service.marcarEntregado(itemId, request, responsableId))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void registrarDevolucion_itemEntregado_loMarcaDevueltoYReportaAuditoria() {
        UUID itemId = UUID.randomUUID();
        UUID organizadorId = UUID.randomUUID();
        ItemDotacion entregado = ItemDotacion.builder()
                .id(itemId)
                .equipoId(equipoId)
                .tipoItem(TipoItemDotacion.KIT)
                .cantidad(1)
                .estado(EstadoDotacion.ENTREGADO)
                .responsableAsignadoId(responsableId)
                .entregadoPorId(responsableId)
                .fechaRegistro(Instant.now())
                .fechaEntrega(Instant.now())
                .build();

        when(repository.findById(itemId)).thenReturn(Optional.of(entregado));
        when(repository.save(any(ItemDotacion.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemDotacionResponse response = service.registrarDevolucion(
                itemId, new RegistrarDevolucionDotacionRequest("Devuelto completo"), organizadorId);

        assertThat(response.estado()).isEqualTo(EstadoDotacion.DEVUELTO);
        assertThat(response.recibidoPorId()).isEqualTo(organizadorId);
        assertThat(response.fechaDevolucion()).isNotNull();
        verify(auditoriaClientPort, times(1)).reportarEntrega(any(RegistroAuditoriaDTO.class));
    }

    @Test
    void registrarDevolucion_itemPendiente_lanzaDuplicateResource() {
        UUID itemId = UUID.randomUUID();
        ItemDotacion pendiente = ItemDotacion.builder()
                .id(itemId)
                .equipoId(equipoId)
                .tipoItem(TipoItemDotacion.KIT)
                .cantidad(1)
                .estado(EstadoDotacion.PENDIENTE)
                .responsableAsignadoId(responsableId)
                .fechaRegistro(Instant.now())
                .build();

        when(repository.findById(itemId)).thenReturn(Optional.of(pendiente));
        RegistrarDevolucionDotacionRequest request = new RegistrarDevolucionDotacionRequest(null);

        assertThatThrownBy(() -> service.registrarDevolucion(itemId, request, responsableId))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
        verify(auditoriaClientPort, never()).reportarEntrega(any());
    }

    @Test
    void registrarDevolucion_itemYaDevuelto_lanzaDuplicateResource() {
        UUID itemId = UUID.randomUUID();
        ItemDotacion devuelto = ItemDotacion.builder()
                .id(itemId)
                .equipoId(equipoId)
                .tipoItem(TipoItemDotacion.KIT)
                .cantidad(1)
                .estado(EstadoDotacion.DEVUELTO)
                .responsableAsignadoId(responsableId)
                .fechaRegistro(Instant.now())
                .build();

        when(repository.findById(itemId)).thenReturn(Optional.of(devuelto));
        RegistrarDevolucionDotacionRequest request = new RegistrarDevolucionDotacionRequest(null);

        assertThatThrownBy(() -> service.registrarDevolucion(itemId, request, responsableId))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void registrarDevolucion_itemInexistente_lanzaRecursoNoEncontrado() {
        UUID itemId = UUID.randomUUID();
        when(repository.findById(itemId)).thenReturn(Optional.empty());
        RegistrarDevolucionDotacionRequest request = new RegistrarDevolucionDotacionRequest(null);

        assertThatThrownBy(() -> service.registrarDevolucion(itemId, request, responsableId))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void listarPorEquipo_sinFiltroDeEstado_usaFindByEquipoId() {
        ItemDotacion item = ItemDotacion.builder()
                .id(UUID.randomUUID())
                .equipoId(equipoId)
                .tipoItem(TipoItemDotacion.PETO)
                .cantidad(1)
                .estado(EstadoDotacion.PENDIENTE)
                .responsableAsignadoId(responsableId)
                .fechaRegistro(Instant.now())
                .build();
        when(repository.findByEquipoId(equipoId)).thenReturn(java.util.List.of(item));

        var result = service.listarPorEquipo(equipoId, null);

        assertThat(result).hasSize(1);
    }

    @Test
    void listarPorEquipo_conFiltroDeEstado_usaFindByEquipoIdAndEstado() {
        when(repository.findByEquipoIdAndEstado(equipoId, EstadoDotacion.ENTREGADO))
                .thenReturn(java.util.List.of());

        var result = service.listarPorEquipo(equipoId, EstadoDotacion.ENTREGADO);

        assertThat(result).isEmpty();
    }
}
