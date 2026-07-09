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

        assertThatThrownBy(() -> service.marcarEntregado(
                itemId, new MarcarDotacionEntregadaRequest(null), responsableId))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
        verify(auditoriaClientPort, never()).reportarEntrega(any());
    }

    @Test
    void marcarEntregado_itemInexistente_lanzaRecursoNoEncontrado() {
        UUID itemId = UUID.randomUUID();
        when(repository.findById(itemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.marcarEntregado(
                itemId, new MarcarDotacionEntregadaRequest(null), responsableId))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
