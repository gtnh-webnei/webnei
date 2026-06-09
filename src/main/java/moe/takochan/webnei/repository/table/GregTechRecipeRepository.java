package moe.takochan.webnei.repository.table;

import java.util.List;
import moe.takochan.webnei.model.entity.table.GregTechRecipeEntity;
import moe.takochan.webnei.model.entity.table.RecipeSearchDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GregTechRecipeRepository
        extends JpaRepository<GregTechRecipeEntity, GregTechRecipeEntity.GregTechRecipeId> {

    List<GregTechRecipeEntity> findByDatasetIdAndRecipeIdIn(String datasetId, List<String> recipeIds);

    interface CategoryVoltageTierCount {
        String getTier();
        long getRecipeCount();
    }

    @Query("""
            select g.voltageTier as tier, count(g.recipeId) as recipeCount
            from GregTechRecipeEntity g
            where g.datasetId = :datasetId
              and g.voltageTier is not null
              and exists (
                select 1
                from RecipeSearchDocumentEntity d
                where d.datasetId = g.datasetId
                  and d.recipeId = g.recipeId
                  and d.categoryId = :categoryId
                  and d.searchText like :pattern
              )
            group by g.voltageTier
            order by min(g.voltage) asc, g.voltageTier asc
            """)
    List<CategoryVoltageTierCount> findVoltageTiersByCategoryAndSearch(
            @Param("datasetId") String datasetId,
            @Param("categoryId") String categoryId,
            @Param("pattern") String pattern);
}
