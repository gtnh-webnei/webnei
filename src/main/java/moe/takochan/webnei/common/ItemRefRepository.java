package moe.takochan.webnei.common;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRefRepository extends JpaRepository<ItemRefEntity, ItemRefEntity.ItemRefId> {

    List<ItemRefEntity> findByDatasetIdAndItemVariantIdIn(String datasetId, List<String> itemVariantIds);
}
