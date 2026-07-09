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
import co.edu.escuelaing.techcup.logistics.dto.request.RegistrarEntregaRefrigerioRequest;
import co.edu.escuelaing.techcup.logistics.dto.response.EntregaRefrigerioResponse;
import co.edu.escuelaing.techcup.logistics.entity.DefinicionRefrigerio;
import co.edu.escuelaing.techcup.logistics.entity.EntregaRefrigerio;
import co.edu.escuelaing.techcup.logistics.enums.TipoDestinatario;
import co.edu.escuelaing.techcup.logistics.exception.DuplicateResourceException;
import co.edu.escuelaing.techcup.logistics.exception.RecursoNoEncontradoException;
import co.edu.escuelaing.techcup.logistics.repository.DefinicionRefrigerioRepository;
import co.edu.escuelaing.techcup.logistics.repository.EntregaRefrigerioRepository;

@ExtendWith(MockitoExtension.class)
class EntregaRefrigerioServiceImplTest {

    @Mock
    private EntregaRefrigerioRepository repository;

    @Mock
    private DefinicionRefrigerioRepository definicionRepository;

    @Mock
    private EquipoClientPort equipoClientPort;

    @Mock
    private AuditoriaClientPort auditoriaClientPort;

    @InjectMocks
    private EntregaRefrigerioServiceImpl service;

    private UUID partidoId;
    private UUID equipoId;
    private UUID definicionId;
    private UUID responsableId;
    private DefinicionRefrigerio definicion;

    @BeforeEach
    void setUp() {
        partidoId = UUID.randomUUID();
        equipoId = UUID.randomUUID();
        definicionId = UUID.randomUUID();
        responsableId = UUID.randomUUID();

        definicion = DefinicionRefrigerio.builder()
                .id(definicionId)
                .partidoId(partidoId)
                .equipoId(equipoId)
                .fechaCreacion(Instant.now())
                .creadoPorId(UUID.randomUUID())
                .build();
    }

    @Test
    void registrar_entregaAEquipoValido_guardaYReportaAuditoria() {
        RegistrarEntregaRefrigerioRequest request = new RegistrarEntregaRefrigerioRequest(
                definicionId, TipoDestinatario.EQUIPO, equipoId, null);

        when(definicionRepository.findById(definicionId)).thenReturn(Optional.of(definicion));
        when(equipoClientPort.existeEquipo(equipoId)).thenReturn(true);
        when(repository.existsByPartidoIdAndTipoDestinatarioAndDestinatarioId(
                partidoId, TipoDestinatario.EQUIPO, equipoId)).thenReturn(false);
        when(repository.save(any(EntregaRefrigerio.class))).thenAnswer(inv -> {
            EntregaRefrigerio entity = inv.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        EntregaRefrigerioResponse response = service.registrar(request, responsableId);

        assertThat(response.partidoId()).isEqualTo(partidoId);
        assertThat(response.responsableId()).isEqualTo(responsableId);
        verify(auditoriaClientPort, times(1)).reportarEntrega(any(RegistroAuditoriaDTO.class));
    }

    @Test
    void registrar_definicionInexistente_lanzaRecursoNoEncontrado() {
        RegistrarEntregaRefrigerioRequest request = new RegistrarEntregaRefrigerioRequest(
                definicionId, TipoDestinatario.EQUIPO, equipoId, null);
        when(definicionRepository.findById(definicionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(request, responsableId))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void registrar_jugadorNoPerteneceAlEquipo_lanzaRecursoNoEncontrado() {
        UUID jugadorId = UUID.randomUUID();
        RegistrarEntregaRefrigerioRequest request = new RegistrarEntregaRefrigerioRequest(
                definicionId, TipoDestinatario.JUGADOR, jugadorId, null);

        when(definicionRepository.findById(definicionId)).thenReturn(Optional.of(definicion));
        when(equipoClientPort.existeJugadorEnEquipo(jugadorId, equipoId)).thenReturn(false);

        assertThatThrownBy(() -> service.registrar(request, responsableId))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void registrar_entregaDuplicadaParaMismoDestinatarioYPartido_lanzaDuplicateResource() {
        RegistrarEntregaRefrigerioRequest request = new RegistrarEntregaRefrigerioRequest(
                definicionId, TipoDestinatario.EQUIPO, equipoId, null);

        when(definicionRepository.findById(definicionId)).thenReturn(Optional.of(definicion));
        when(equipoClientPort.existeEquipo(equipoId)).thenReturn(true);
        when(repository.existsByPartidoIdAndTipoDestinatarioAndDestinatarioId(
                partidoId, TipoDestinatario.EQUIPO, equipoId)).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(request, responsableId))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
        verify(auditoriaClientPort, never()).reportarEntrega(any());
    }
}
