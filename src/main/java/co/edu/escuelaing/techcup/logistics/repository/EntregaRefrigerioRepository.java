package co.edu.escuelaing.techcup.logistics.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.escuelaing.techcup.logistics.entity.EntregaRefrigerio;
import co.edu.escuelaing.techcup.logistics.enums.TipoDestinatario;

public interface EntregaRefrigerioRepository extends JpaRepository<EntregaRefrigerio, UUID> {

    boolean existsByPartidoIdAndTipoDestinatarioAndDestinatarioId(
            UUID partidoId, TipoDestinatario tipoDestinatario, UUID destinatarioId);

    List<EntregaRefrigerio> findByPartidoId(UUID partidoId);

    List<EntregaRefrigerio> findByDefinicionRefrigerioId(UUID definicionRefrigerioId);
}
