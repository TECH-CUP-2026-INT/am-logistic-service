package co.edu.escuelaing.techcup.logistics.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.escuelaing.techcup.logistics.adapter.equipos.EquipoClientPort;
import co.edu.escuelaing.techcup.logistics.adapter.torneos.TorneoClientPort;
import co.edu.escuelaing.techcup.logistics.dto.request.CrearDefinicionRefrigerioRequest;
import co.edu.escuelaing.techcup.logistics.dto.request.ItemRefrigerioRequest;
import co.edu.escuelaing.techcup.logistics.dto.response.DefinicionRefrigerioResponse;
import co.edu.escuelaing.techcup.logistics.entity.DefinicionRefrigerio;
import co.edu.escuelaing.techcup.logistics.exception.DuplicateResourceException;
import co.edu.escuelaing.techcup.logistics.exception.RecursoNoEncontradoException;
import co.edu.escuelaing.techcup.logistics.repository.DefinicionRefrigerioRepository;

@ExtendWith(MockitoExtension.class)
class DefinicionRefrigerioServiceImplTest {

    @Mock
    private DefinicionRefrigerioRepository repository;

    @Mock
    private TorneoClientPort torneoClientPort;

    @Mock
    private EquipoClientPort equipoClientPort;

    @InjectMocks
    private DefinicionRefrigerioServiceImpl service;

    private UUID partidoId;
    private UUID equipoId;
    private UUID creadoPorId;
    private CrearDefinicionRefrigerioRequest request;

    @BeforeEach
    void setUp() {
        partidoId = UUID.randomUUID();
        equipoId = UUID.randomUUID();
        creadoPorId = UUID.randomUUID();
        request = new CrearDefinicionRefrigerioRequest(
                partidoId,
                equipoId,
                List.of(new ItemRefrigerioRequest("Sandwich + jugo", 15)),
                "Entregar antes del calentamiento");
    }

    @Test
    void crear_conDatosValidos_guardaYRetornaDefinicion() {
        when(torneoClientPort.existePartido(partidoId)).thenReturn(true);
        when(equipoClientPort.existeEquipo(equipoId)).thenReturn(true);
        when(repository.existsByPartidoIdAndEquipoId(partidoId, equipoId)).thenReturn(false);
        when(repository.save(any(DefinicionRefrigerio.class))).thenAnswer(inv -> {
            DefinicionRefrigerio entity = inv.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        DefinicionRefrigerioResponse response = service.crear(request, creadoPorId);

        assertThat(response.id()).isNotNull();
        assertThat(response.partidoId()).isEqualTo(partidoId);
        assertThat(response.equipoId()).isEqualTo(equipoId);
        assertThat(response.items()).hasSize(1);
        assertThat(response.creadoPorId()).isEqualTo(creadoPorId);
    }

    @Test
    void crear_partidoInexistenteEnTorneos_lanzaRecursoNoEncontrado() {
        when(torneoClientPort.existePartido(partidoId)).thenReturn(false);

        assertThatThrownBy(() -> service.crear(request, creadoPorId))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void crear_equipoInexistenteEnEquipos_lanzaRecursoNoEncontrado() {
        when(torneoClientPort.existePartido(partidoId)).thenReturn(true);
        when(equipoClientPort.existeEquipo(equipoId)).thenReturn(false);

        assertThatThrownBy(() -> service.crear(request, creadoPorId))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void crear_definicionYaExistenteParaPartidoYEquipo_lanzaDuplicateResource() {
        when(torneoClientPort.existePartido(partidoId)).thenReturn(true);
        when(equipoClientPort.existeEquipo(equipoId)).thenReturn(true);
        when(repository.existsByPartidoIdAndEquipoId(partidoId, equipoId)).thenReturn(true);

        assertThatThrownBy(() -> service.crear(request, creadoPorId))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void listarPorPartido_retornaDefinicionesMapeadas() {
        DefinicionRefrigerio definicion = DefinicionRefrigerio.builder()
                .id(UUID.randomUUID())
                .partidoId(partidoId)
                .equipoId(equipoId)
                .items(List.of())
                .creadoPorId(creadoPorId)
                .fechaCreacion(java.time.Instant.now())
                .build();
        when(repository.findByPartidoId(partidoId)).thenReturn(List.of(definicion));

        List<DefinicionRefrigerioResponse> result = service.listarPorPartido(partidoId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).partidoId()).isEqualTo(partidoId);
    }
}
