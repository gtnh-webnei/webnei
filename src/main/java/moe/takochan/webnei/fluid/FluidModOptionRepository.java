package moe.takochan.webnei.fluid;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FluidModOptionRepository
        extends JpaRepository<FluidModOptionEntity, FluidModOptionEntity.ModOptionId> {

    List<FluidModOptionEntity> findByDatasetIdOrderByNameAscModIdAsc(String datasetId);

    Optional<FluidModOptionEntity> findByDatasetIdAndModId(String datasetId, String modId);
}
