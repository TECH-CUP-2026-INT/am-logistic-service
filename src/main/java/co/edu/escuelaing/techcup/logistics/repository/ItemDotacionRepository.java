package co.edu.escuelaing.techcup.logistics.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.escuelaing.techcup.logistics.entity.ItemDotacion;
import co.edu.escuelaing.techcup.logistics.enums.EstadoDotacion;

public interface ItemDotacionRepository extends JpaRepository<ItemDotacion, UUID> {

    List<ItemDotacion> findByEquipoId(UUID equipoId);

    List<ItemDotacion> findByEquipoIdAndEstado(UUID equipoId, EstadoDotacion estado);
}
