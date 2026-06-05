package moe.takochan.webnei.gtore;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GtOreVeinLayerRepository
        extends JpaRepository<GtOreVeinLayerEntity, GtOreVeinLayerEntity.OreVeinLayerId> {

    List<GtOreVeinLayerEntity> findByDatasetIdAndVeinName(String datasetId, String veinName, Sort sort);
}
