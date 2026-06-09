package moe.takochan.webnei.repository.view;

import java.util.List;
import moe.takochan.webnei.model.entity.view.GtUndergroundFluidBrowserEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GtUndergroundFluidBrowserRepository
        extends JpaRepository<GtUndergroundFluidBrowserEntity, GtUndergroundFluidBrowserEntity.UndergroundFluidBrowserId>,
                JpaSpecificationExecutor<GtUndergroundFluidBrowserEntity> {

    List<GtUndergroundFluidBrowserEntity> findByDatasetIdAndFluidId(
            String datasetId, String fluidId, Sort sort);
}
