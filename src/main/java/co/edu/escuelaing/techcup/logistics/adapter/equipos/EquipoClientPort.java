package co.edu.escuelaing.techcup.logistics.adapter.equipos;

import java.util.UUID;

/**
 * Puerto hacia el Servicio de Equipos: permite validar que un equipo o jugador
 * destinatario de una entrega existe (y, en el caso de jugador, que pertenece
 * al equipo indicado).
 */
public interface EquipoClientPort {

    boolean existeEquipo(UUID equipoId);

    boolean existeJugadorEnEquipo(UUID jugadorId, UUID equipoId);
}
