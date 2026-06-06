package moe.takochan.webnei.gtore;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GtOreVeinDimensionRepository
        extends JpaRepository<GtOreVeinDimensionEntity, GtOreVeinDimensionEntity.OreVeinDimensionId> {

    List<GtOreVeinDimensionEntity> findByDatasetIdAndVeinNameAndEnabled(
            String datasetId, String veinName, boolean enabled, Sort sort);

    List<GtOreVeinDimensionEntity> findByDatasetIdAndEnabled(String datasetId, boolean enabled);
}
