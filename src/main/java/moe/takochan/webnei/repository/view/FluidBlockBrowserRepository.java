package moe.takochan.webnei.repository.view;

import java.util.List;
import moe.takochan.webnei.model.entity.view.FluidBlockBrowserEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FluidBlockBrowserRepository
        extends JpaRepository<FluidBlockBrowserEntity, FluidBlockBrowserEntity.FluidBlockId> {

    List<FluidBlockBrowserEntity> findByDatasetIdAndFluidVariantId(String datasetId, String fluidVariantId, Sort sort);
}
