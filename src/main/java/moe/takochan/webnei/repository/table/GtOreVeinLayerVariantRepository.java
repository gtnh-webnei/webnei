package moe.takochan.webnei.repository.table;

import java.util.List;
import moe.takochan.webnei.model.entity.table.GtOreVeinLayerVariantEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GtOreVeinLayerVariantRepository
        extends JpaRepository<GtOreVeinLayerVariantEntity, GtOreVeinLayerVariantEntity.OreVeinLayerVariantId> {

    List<GtOreVeinLayerVariantEntity> findByDatasetIdAndVeinName(String datasetId, String veinName, Sort sort);

    List<GtOreVeinLayerVariantEntity> findByDatasetIdAndItemVariantId(String datasetId, String itemVariantId, Sort sort);
}
