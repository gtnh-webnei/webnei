package moe.takochan.webnei.gtore;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GtOreVeinLayerVariantRepository
        extends JpaRepository<GtOreVeinLayerVariantEntity, GtOreVeinLayerVariantEntity.OreVeinLayerVariantId> {

    List<GtOreVeinLayerVariantEntity> findByDatasetIdAndVeinName(String datasetId, String veinName, Sort sort);

    List<GtOreVeinLayerVariantEntity> findByDatasetIdAndItemVariantId(String datasetId, String itemVariantId, Sort sort);
}
