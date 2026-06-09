package moe.takochan.webnei.repository.table;

import moe.takochan.webnei.model.entity.table.GtOreSmallEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GtOreSmallRepository
        extends JpaRepository<GtOreSmallEntity, GtOreSmallEntity.OreSmallId>,
                JpaSpecificationExecutor<GtOreSmallEntity> {

    java.util.List<GtOreSmallEntity> findByDatasetIdAndSmallOreItemVariantId(
            String datasetId, String smallOreItemVariantId, org.springframework.data.domain.Sort sort);
}
