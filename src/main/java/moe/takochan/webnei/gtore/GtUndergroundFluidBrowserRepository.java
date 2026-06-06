package moe.takochan.webnei.gtore;

import java.util.List;

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
