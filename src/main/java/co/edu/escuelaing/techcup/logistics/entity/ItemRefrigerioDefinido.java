package co.edu.escuelaing.techcup.logistics.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Un refrigerio concreto (ej. "sandwich + jugo") dentro de una {@link DefinicionRefrigerio}.
 * Una definicion puede agrupar varios items para el mismo partido/equipo.
 * Se serializa como sub-documento embebido dentro de DefinicionRefrigerio.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRefrigerioDefinido {

    private String descripcion;

    private Integer cantidad;
}
