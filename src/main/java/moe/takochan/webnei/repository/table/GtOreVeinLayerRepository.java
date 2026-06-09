package moe.takochan.webnei.repository.table;

import java.util.List;
import moe.takochan.webnei.model.entity.table.GtOreVeinLayerEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GtOreVeinLayerRepository
        extends JpaRepository<GtOreVeinLayerEntity, GtOreVeinLayerEntity.OreVeinLayerId> {

    List<GtOreVeinLayerEntity> findByDatasetIdAndVeinName(String datasetId, String veinName, Sort sort);

    List<GtOreVeinLayerEntity> findByDatasetIdAndBlockItemVariantId(String datasetId, String blockItemVariantId, Sort sort);

    List<GtOreVeinLayerEntity> findByDatasetIdAndMaterialNameIn(String datasetId, List<String> materialNames, Sort sort);
}
