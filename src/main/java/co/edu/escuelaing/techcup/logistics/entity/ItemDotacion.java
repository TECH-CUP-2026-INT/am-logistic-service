package co.edu.escuelaing.techcup.logistics.entity;

import java.time.Instant;
import java.util.UUID;

import co.edu.escuelaing.techcup.logistics.enums.EstadoDotacion;
import co.edu.escuelaing.techcup.logistics.enums.TipoItemDotacion;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
@Document(collection = "item_dotacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class ItemDotacion {

    @Id
    @Builder.Default
    @EqualsAndHashCode.Include
    private UUID id = UUID.randomUUID();

    private UUID equipoId;

    private TipoItemDotacion tipoItem;

    private Integer cantidad;

    private EstadoDotacion estado;

    private UUID responsableAsignadoId;

    private UUID entregadoPorId;

    private Instant fechaRegistro;

    private Instant fechaEntrega;

    private UUID recibidoPorId;

    private Instant fechaDevolucion;

    private String observaciones;
}
