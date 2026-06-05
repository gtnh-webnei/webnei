package moe.takochan.webnei.gtore;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GtOreSmallDimensionRepository
        extends JpaRepository<GtOreSmallDimensionEntity, GtOreSmallDimensionEntity.OreSmallDimensionId> {

    List<GtOreSmallDimensionEntity> findByDatasetIdAndOreGenNameAndEnabled(
            String datasetId, String oreGenName, boolean enabled, Sort sort);
}
