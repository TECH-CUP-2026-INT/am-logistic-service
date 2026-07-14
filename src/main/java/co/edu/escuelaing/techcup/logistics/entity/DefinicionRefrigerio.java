package co.edu.escuelaing.techcup.logistics.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
 * Define que refrigerio(s) corresponden a un equipo para un partido especifico.
 * Unica por (partidoId, equipoId): el organizador no puede duplicar la definicion
 * para el mismo partido/equipo, aunque puede agrupar varios items en una misma definicion.
 */
@Document(collection = "definicion_refrigerio")
@CompoundIndex(
        name = "uq_definicion_refrigerio_partido_equipo",
        def = "{'partidoId': 1, 'equipoId': 1}",
        unique = true
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
    @Builder.Default
    @EqualsAndHashCode.Include
    private UUID id = UUID.randomUUID();

    private UUID partidoId;

    private UUID equipoId;

    @Builder.Default
    private List<ItemRefrigerioDefinido> items = new ArrayList<>();

    private String observaciones;

    private UUID creadoPorId;

    private Instant fechaCreacion;
}
