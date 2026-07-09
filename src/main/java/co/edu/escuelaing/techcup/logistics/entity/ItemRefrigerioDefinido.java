package co.edu.escuelaing.techcup.logistics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Un refrigerio concreto (ej. "sandwich + jugo") dentro de una {@link DefinicionRefrigerio}.
 * Una definicion puede agrupar varios items para el mismo partido/equipo.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRefrigerioDefinido {

    @Column(name = "descripcion", nullable = false, length = 200)
    private String descripcion;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;
}
