package moe.takochan.webnei.recipe;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GregTechRecipeRepository
        extends JpaRepository<GregTechRecipeEntity, GregTechRecipeEntity.GregTechRecipeId> {

    List<GregTechRecipeEntity> findByDatasetIdAndRecipeIdIn(String datasetId, List<String> recipeIds);
}
