package moe.takochan.webnei.repository.table;

import java.util.List;
import moe.takochan.webnei.model.entity.table.NeiTextureExportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NeiTextureExportRepository
        extends JpaRepository<NeiTextureExportEntity, NeiTextureExportEntity.NeiTextureExportId> {

    List<NeiTextureExportEntity> findByDatasetIdAndResourceIn(String datasetId, List<String> resources);
}
