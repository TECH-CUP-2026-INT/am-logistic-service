package co.edu.escuelaing.techcup.logistics.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Define que refrigerio(s) corresponden a un equipo para un partido especifico.
 * Unica por (partidoId, equipoId): el organizador no puede duplicar la definicion
 * para el mismo partido/equipo, aunque puede agrupar varios items en una misma definicion.
 */
@Entity
@Table(
        name = "definicion_refrigerio",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_definicion_refrigerio_partido_equipo",
                columnNames = {"partido_id", "equipo_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class DefinicionRefrigerio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "partido_id", nullable = false)
    private UUID partidoId;

    @Column(name = "equipo_id", nullable = false)
    private UUID equipoId;

    @Builder.Default
    @ElementCollection
    @CollectionTable(
            name = "item_refrigerio_definido",
            joinColumns = @JoinColumn(name = "definicion_refrigerio_id")
    )
    private List<ItemRefrigerioDefinido> items = new ArrayList<>();

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "creado_por_id", nullable = false)
    private UUID creadoPorId;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;
}
