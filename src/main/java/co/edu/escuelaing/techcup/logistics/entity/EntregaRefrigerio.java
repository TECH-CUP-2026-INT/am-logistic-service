package co.edu.escuelaing.techcup.logistics.entity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Registro de la entrega real de un refrigerio al Capitan del equipo clasificado,
 * para un partido especifico. Unica por (partidoId, capitanId): un mismo capitan
 * no puede recibir dos entregas registradas para el mismo partido.
 */
@Document(collection = "entrega_refrigerio")
@CompoundIndex(
        name = "uq_entrega_refrigerio_capitan",
        def = "{'partidoId': 1, 'capitanId': 1}",
        unique = true
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class EntregaRefrigerio {

    @Id
    @Builder.Default
    @EqualsAndHashCode.Include
    private UUID id = UUID.randomUUID();

    private UUID definicionRefrigerioId;

    private UUID partidoId;

    private UUID equipoId;

    private UUID capitanId;

    private UUID responsableId;

    private Instant fechaEntrega;

    private String observaciones;
}
