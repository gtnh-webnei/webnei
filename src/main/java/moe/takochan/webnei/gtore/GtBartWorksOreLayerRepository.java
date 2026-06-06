package moe.takochan.webnei.gtore;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GtBartWorksOreLayerRepository
        extends JpaRepository<GtBartWorksOreLayerEntity, GtBartWorksOreLayerEntity.BartWorksOreLayerId> {

    List<GtBartWorksOreLayerEntity> findByDatasetIdAndEntryId(String datasetId, String entryId, Sort sort);

    List<GtBartWorksOreLayerEntity> findByDatasetIdAndItemVariantId(String datasetId, String itemVariantId, Sort sort);
}
