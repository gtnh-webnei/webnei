package moe.takochan.webnei.repository.view;

import java.util.List;
import moe.takochan.webnei.model.entity.view.FluidContainerBrowserEntity;
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
