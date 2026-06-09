package moe.takochan.webnei.repository.table;

import java.util.List;
import moe.takochan.webnei.model.entity.table.GregTechSpecialItemEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GregTechSpecialItemRepository
        extends JpaRepository<GregTechSpecialItemEntity, GregTechSpecialItemEntity.SpecialItemId> {

    List<GregTechSpecialItemEntity> findByDatasetIdAndRecipeIdIn(String datasetId, List<String> recipeIds, Sort sort);
}
