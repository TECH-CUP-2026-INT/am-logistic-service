package co.edu.escuelaing.techcup.logistics.entity;

import java.time.Instant;
import java.util.UUID;

import co.edu.escuelaing.techcup.logistics.enums.TipoDestinatario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
 * Registro de la entrega real de un refrigerio a un destinatario (equipo o jugador)
 * para un partido especifico. Unica por (partidoId, tipoDestinatario, destinatarioId):
 * un mismo destinatario no puede recibir dos entregas registradas para el mismo partido.
 */
@Entity
@Table(
        name = "entrega_refrigerio",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_entrega_refrigerio_destinatario",
                columnNames = {"partido_id", "tipo_destinatario", "destinatario_id"}
        )
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
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "definicion_refrigerio_id", nullable = false)
    private UUID definicionRefrigerioId;

    @Column(name = "partido_id", nullable = false)
    private UUID partidoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_destinatario", nullable = false, length = 20)
    private TipoDestinatario tipoDestinatario;

    @Column(name = "destinatario_id", nullable = false)
    private UUID destinatarioId;

    @Column(name = "responsable_id", nullable = false)
    private UUID responsableId;

    @Column(name = "fecha_entrega", nullable = false)
    private Instant fechaEntrega;

    @Column(name = "observaciones", length = 500)
    private String observaciones;
}
