package moe.takochan.webnei.fluid;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FluidVariantRepository
        extends JpaRepository<FluidVariantBrowserEntity, FluidVariantBrowserEntity.FluidVariantId>,
                JpaSpecificationExecutor<FluidVariantBrowserEntity> {

    List<FluidVariantBrowserEntity> findByDatasetIdAndFluidVariantIdIn(String datasetId, List<String> fluidVariantIds);
}
