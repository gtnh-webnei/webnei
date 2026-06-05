package moe.takochan.webnei.recipe;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeSlotBrowserRepository
        extends JpaRepository<RecipeSlotBrowserEntity, RecipeSlotBrowserEntity.SlotBrowserId> {

    List<RecipeSlotBrowserEntity> findByDatasetIdAndRecipeIdIn(String datasetId, List<String> recipeIds, Sort sort);
}
