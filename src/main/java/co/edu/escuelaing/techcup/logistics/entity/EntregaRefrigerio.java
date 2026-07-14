package co.edu.escuelaing.techcup.logistics.entity;

import java.time.Instant;
import java.util.UUID;

import co.edu.escuelaing.techcup.logistics.enums.TipoDestinatario;
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
 * Registro de la entrega real de un refrigerio a un destinatario (equipo o jugador)
 * para un partido especifico. Unica por (partidoId, tipoDestinatario, destinatarioId):
 * un mismo destinatario no puede recibir dos entregas registradas para el mismo partido.
 */
@Document(collection = "entrega_refrigerio")
@CompoundIndex(
        name = "uq_entrega_refrigerio_destinatario",
        def = "{'partidoId': 1, 'tipoDestinatario': 1, 'destinatarioId': 1}",
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

    private TipoDestinatario tipoDestinatario;

    private UUID destinatarioId;

    private UUID responsableId;

    private Instant fechaEntrega;

    private String observaciones;
}
