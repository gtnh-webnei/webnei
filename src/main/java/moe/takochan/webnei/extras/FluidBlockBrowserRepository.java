package moe.takochan.webnei.extras;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FluidBlockBrowserRepository
        extends JpaRepository<FluidBlockBrowserEntity, FluidBlockBrowserEntity.FluidBlockId> {

    List<FluidBlockBrowserEntity> findByDatasetIdAndFluidVariantId(String datasetId, String fluidVariantId, Sort sort);
}
