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
    private UUID capitanId;
    private DefinicionRefrigerio definicion;

    @BeforeEach
    void setUp() {
        partidoId = UUID.randomUUID();
        equipoId = UUID.randomUUID();
        definicionId = UUID.randomUUID();
        responsableId = UUID.randomUUID();
        capitanId = UUID.randomUUID();

        definicion = DefinicionRefrigerio.builder()
                .id(definicionId)
                .partidoId(partidoId)
                .equipoId(equipoId)
                .fechaCreacion(Instant.now())
                .creadoPorId(UUID.randomUUID())
                .build();
    }

    @Test
    void registrar_capitanValido_guardaYReportaAuditoria() {
        RegistrarEntregaRefrigerioRequest request = new RegistrarEntregaRefrigerioRequest(
                definicionId, capitanId, null);

        when(definicionRepository.findById(definicionId)).thenReturn(Optional.of(definicion));
        when(equipoClientPort.esCapitanDelEquipo(capitanId, equipoId)).thenReturn(true);
        when(repository.existsByPartidoIdAndCapitanId(partidoId, capitanId)).thenReturn(false);
        when(repository.save(any(EntregaRefrigerio.class))).thenAnswer(inv -> {
            EntregaRefrigerio entity = inv.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        EntregaRefrigerioResponse response = service.registrar(request, responsableId);

        assertThat(response.partidoId()).isEqualTo(partidoId);
        assertThat(response.equipoId()).isEqualTo(equipoId);
        assertThat(response.capitanId()).isEqualTo(capitanId);
        assertThat(response.responsableId()).isEqualTo(responsableId);
        verify(auditoriaClientPort, times(1)).reportarEntrega(any(RegistroAuditoriaDTO.class));
    }

    @Test
    void registrar_definicionInexistente_lanzaRecursoNoEncontrado() {
        RegistrarEntregaRefrigerioRequest request = new RegistrarEntregaRefrigerioRequest(
                definicionId, capitanId, null);
        when(definicionRepository.findById(definicionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(request, responsableId))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void registrar_jugadorNoEsCapitanDelEquipo_lanzaRecursoNoEncontrado() {
        UUID jugadorId = UUID.randomUUID();
        RegistrarEntregaRefrigerioRequest request = new RegistrarEntregaRefrigerioRequest(
                definicionId, jugadorId, null);

        when(definicionRepository.findById(definicionId)).thenReturn(Optional.of(definicion));
        when(equipoClientPort.esCapitanDelEquipo(jugadorId, equipoId)).thenReturn(false);

        assertThatThrownBy(() -> service.registrar(request, responsableId))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void registrar_entregaDuplicadaParaMismoCapitanYPartido_lanzaDuplicateResource() {
        RegistrarEntregaRefrigerioRequest request = new RegistrarEntregaRefrigerioRequest(
                definicionId, capitanId, null);

        when(definicionRepository.findById(definicionId)).thenReturn(Optional.of(definicion));
        when(equipoClientPort.esCapitanDelEquipo(capitanId, equipoId)).thenReturn(true);
        when(repository.existsByPartidoIdAndCapitanId(partidoId, capitanId)).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(request, responsableId))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
        verify(auditoriaClientPort, never()).reportarEntrega(any());
    }

    @Test
    void listarPorPartido_retornaEntregasMapeadas() {
        EntregaRefrigerio entrega = EntregaRefrigerio.builder()
                .id(UUID.randomUUID())
                .definicionRefrigerioId(definicionId)
                .partidoId(partidoId)
                .equipoId(equipoId)
                .capitanId(capitanId)
                .responsableId(responsableId)
                .fechaEntrega(Instant.now())
                .build();
        when(repository.findByPartidoId(partidoId)).thenReturn(java.util.List.of(entrega));

        var result = service.listarPorPartido(partidoId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).partidoId()).isEqualTo(partidoId);
    }
}
