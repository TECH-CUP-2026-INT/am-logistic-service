package co.edu.escuelaing.techcup.logistics.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import co.edu.escuelaing.techcup.logistics.entity.DefinicionRefrigerio;

public interface DefinicionRefrigerioRepository extends MongoRepository<DefinicionRefrigerio, UUID> {

    boolean existsByPartidoIdAndEquipoId(UUID partidoId, UUID equipoId);

    Optional<DefinicionRefrigerio> findByPartidoIdAndEquipoId(UUID partidoId, UUID equipoId);

    List<DefinicionRefrigerio> findByPartidoId(UUID partidoId);
}
