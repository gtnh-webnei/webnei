package moe.takochan.webnei.recipe;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GregTechRecipeRepository
        extends JpaRepository<GregTechRecipeEntity, GregTechRecipeEntity.GregTechRecipeId> {

    List<GregTechRecipeEntity> findByDatasetIdAndRecipeIdIn(String datasetId, List<String> recipeIds);

    @Query("""
            select g.voltageTier, count(g.recipeId), min(g.voltage)
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
    List<Object[]> findVoltageTiersByCategoryAndSearch(
            @Param("datasetId") String datasetId,
            @Param("categoryId") String categoryId,
            @Param("pattern") String pattern);
}
