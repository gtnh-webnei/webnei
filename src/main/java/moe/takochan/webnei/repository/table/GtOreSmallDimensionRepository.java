package moe.takochan.webnei.repository.table;

import java.util.List;
import moe.takochan.webnei.model.entity.table.GtOreSmallDimensionEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GtOreSmallDimensionRepository
        extends JpaRepository<GtOreSmallDimensionEntity, GtOreSmallDimensionEntity.OreSmallDimensionId> {

    List<GtOreSmallDimensionEntity> findByDatasetIdAndOreGenNameAndEnabled(
            String datasetId, String oreGenName, boolean enabled, Sort sort);

    List<GtOreSmallDimensionEntity> findByDatasetIdAndEnabled(String datasetId, boolean enabled);
}
