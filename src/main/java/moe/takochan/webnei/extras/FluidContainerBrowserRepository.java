package moe.takochan.webnei.extras;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FluidContainerBrowserRepository
        extends JpaRepository<FluidContainerBrowserEntity, FluidContainerBrowserEntity.FluidContainerId>,
                JpaSpecificationExecutor<FluidContainerBrowserEntity> {

    List<FluidContainerBrowserEntity> findByDatasetIdAndFluidVariantId(
            String datasetId, String fluidVariantId, Sort sort);
}
