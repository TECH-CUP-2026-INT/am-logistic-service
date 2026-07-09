package co.edu.escuelaing.techcup.logistics.entity;

import java.time.Instant;
import java.util.UUID;

import co.edu.escuelaing.techcup.logistics.enums.EstadoDotacion;
import co.edu.escuelaing.techcup.logistics.enums.TipoItemDotacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Item de dotacion (peto, balon o kit) asignado a un equipo, con su estado de
 * entrega y el responsable asociado en cada etapa.
 */
@Entity
@Table(name = "item_dotacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class ItemDotacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "equipo_id", nullable = false)
    private UUID equipoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_item", nullable = false, length = 20)
    private TipoItemDotacion tipoItem;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoDotacion estado;

    @Column(name = "responsable_asignado_id", nullable = false)
    private UUID responsableAsignadoId;

    @Column(name = "entregado_por_id")
    private UUID entregadoPorId;

    @Column(name = "fecha_registro", nullable = false)
    private Instant fechaRegistro;

    @Column(name = "fecha_entrega")
    private Instant fechaEntrega;

    @Column(name = "observaciones", length = 500)
    private String observaciones;
}
