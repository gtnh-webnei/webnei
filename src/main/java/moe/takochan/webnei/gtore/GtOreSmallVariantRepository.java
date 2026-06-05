package moe.takochan.webnei.gtore;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GtOreSmallVariantRepository
        extends JpaRepository<GtOreSmallVariantEntity, GtOreSmallVariantEntity.OreSmallVariantId> {

    List<GtOreSmallVariantEntity> findByDatasetIdAndOreGenName(String datasetId, String oreGenName, Sort sort);
}
