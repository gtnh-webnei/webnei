package moe.takochan.webnei.repository.table;

import moe.takochan.webnei.model.entity.table.GtBartWorksOreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GtBartWorksOreRepository
        extends JpaRepository<GtBartWorksOreEntity, GtBartWorksOreEntity.BartWorksOreId>,
                JpaSpecificationExecutor<GtBartWorksOreEntity> {

    java.util.List<GtBartWorksOreEntity> findByDatasetIdAndResultItemVariantId(
            String datasetId, String resultItemVariantId, org.springframework.data.domain.Sort sort);
}
