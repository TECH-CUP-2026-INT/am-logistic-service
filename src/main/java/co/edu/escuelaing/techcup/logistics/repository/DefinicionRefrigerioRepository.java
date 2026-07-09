package co.edu.escuelaing.techcup.logistics.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.escuelaing.techcup.logistics.entity.DefinicionRefrigerio;

public interface DefinicionRefrigerioRepository extends JpaRepository<DefinicionRefrigerio, UUID> {

    boolean existsByPartidoIdAndEquipoId(UUID partidoId, UUID equipoId);

    Optional<DefinicionRefrigerio> findByPartidoIdAndEquipoId(UUID partidoId, UUID equipoId);

    List<DefinicionRefrigerio> findByPartidoId(UUID partidoId);
}
