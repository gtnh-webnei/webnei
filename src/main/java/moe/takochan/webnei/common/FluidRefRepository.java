package moe.takochan.webnei.common;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FluidRefRepository extends JpaRepository<FluidRefEntity, FluidRefEntity.FluidRefId> {

    List<FluidRefEntity> findByDatasetIdAndFluidVariantIdIn(String datasetId, List<String> fluidVariantIds);
}
