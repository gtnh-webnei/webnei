package moe.takochan.webnei.gtore;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GtOreSmallDropRepository
        extends JpaRepository<GtOreSmallDropEntity, GtOreSmallDropEntity.OreSmallDropId> {

    List<GtOreSmallDropEntity> findByDatasetIdAndOreGenName(String datasetId, String oreGenName, Sort sort);
}
