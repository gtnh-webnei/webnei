package moe.takochan.webnei.repository.table;

import java.util.List;
import moe.takochan.webnei.model.entity.table.GtOreSmallDropEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GtOreSmallDropRepository
        extends JpaRepository<GtOreSmallDropEntity, GtOreSmallDropEntity.OreSmallDropId> {

    List<GtOreSmallDropEntity> findByDatasetIdAndOreGenName(String datasetId, String oreGenName, Sort sort);
}
