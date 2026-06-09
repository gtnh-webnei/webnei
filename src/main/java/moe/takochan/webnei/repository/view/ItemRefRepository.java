package moe.takochan.webnei.repository.view;

import java.util.List;
import moe.takochan.webnei.model.entity.view.ItemRefEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRefRepository extends JpaRepository<ItemRefEntity, ItemRefEntity.ItemRefId> {

    List<ItemRefEntity> findByDatasetIdAndItemVariantIdIn(String datasetId, List<String> itemVariantIds);
}
