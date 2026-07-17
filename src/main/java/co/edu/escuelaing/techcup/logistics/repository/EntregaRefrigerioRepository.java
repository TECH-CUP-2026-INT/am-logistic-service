package co.edu.escuelaing.techcup.logistics.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import co.edu.escuelaing.techcup.logistics.entity.EntregaRefrigerio;

public interface EntregaRefrigerioRepository extends MongoRepository<EntregaRefrigerio, UUID> {

    boolean existsByPartidoIdAndCapitanId(UUID partidoId, UUID capitanId);

    List<EntregaRefrigerio> findByPartidoId(UUID partidoId);

    List<EntregaRefrigerio> findByDefinicionRefrigerioId(UUID definicionRefrigerioId);
}
