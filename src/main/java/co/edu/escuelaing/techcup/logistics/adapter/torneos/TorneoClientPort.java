package co.edu.escuelaing.techcup.logistics.adapter.torneos;

import java.util.Optional;
import java.util.UUID;

import co.edu.escuelaing.techcup.logistics.adapter.torneos.dto.PartidoDTO;

/**
 * Puerto hacia el Servicio de Torneos: permite validar que un partido/jornada
 * existe antes de definir o registrar una entrega de refrigerios.
 */
public interface TorneoClientPort {

    boolean existePartido(UUID partidoId);

    Optional<PartidoDTO> obtenerPartido(UUID partidoId);

    /**
     * Indica si el equipo ya clasifico a segunda fase segun los resultados
     * registrados en el Servicio de Torneos. Los refrigerios solo pueden
     * definirse para equipos que cumplan esta condicion.
     */
    boolean equipoClasificadoSegundaFase(UUID equipoId);
}
