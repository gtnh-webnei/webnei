package moe.takochan.webnei.repository.table;

import java.util.List;
import moe.takochan.webnei.model.entity.table.GtBartWorksOreLayerEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GtBartWorksOreLayerRepository
        extends JpaRepository<GtBartWorksOreLayerEntity, GtBartWorksOreLayerEntity.BartWorksOreLayerId> {

    List<GtBartWorksOreLayerEntity> findByDatasetIdAndEntryId(String datasetId, String entryId, Sort sort);

    List<GtBartWorksOreLayerEntity> findByDatasetIdAndItemVariantId(String datasetId, String itemVariantId, Sort sort);
}
