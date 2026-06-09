package moe.takochan.webnei.repository.view;

import java.util.List;
import moe.takochan.webnei.model.entity.view.ItemOreDictionaryNameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemOreDictionaryNameRepository
        extends JpaRepository<ItemOreDictionaryNameEntity, ItemOreDictionaryNameEntity.ItemOreDictionaryNameId> {

    List<ItemOreDictionaryNameEntity> findByDatasetIdAndItemVariantIdOrderByOreNameAsc(String datasetId, String itemVariantId);
}
