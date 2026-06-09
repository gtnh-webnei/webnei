package moe.takochan.webnei.repository.table;

import java.util.List;
import moe.takochan.webnei.model.entity.table.RecipeSlotLayoutEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeSlotLayoutRepository
        extends JpaRepository<RecipeSlotLayoutEntity, RecipeSlotLayoutEntity.SlotLayoutId> {

    List<RecipeSlotLayoutEntity> findByDatasetIdAndCategoryIdIn(String datasetId, List<String> categoryIds, Sort sort);
}
