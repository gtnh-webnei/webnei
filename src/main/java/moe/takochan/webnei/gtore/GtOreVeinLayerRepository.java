package moe.takochan.webnei.gtore;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GtOreVeinLayerRepository
        extends JpaRepository<GtOreVeinLayerEntity, GtOreVeinLayerEntity.OreVeinLayerId> {

    List<GtOreVeinLayerEntity> findByDatasetIdAndVeinName(String datasetId, String veinName, Sort sort);

    List<GtOreVeinLayerEntity> findByDatasetIdAndBlockItemVariantId(String datasetId, String blockItemVariantId, Sort sort);

    List<GtOreVeinLayerEntity> findByDatasetIdAndMaterialName(String datasetId, String materialName, Sort sort);
}
