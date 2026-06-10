package moe.takochan.webnei.repository.table;

import java.util.List;
import moe.takochan.webnei.model.entity.table.RecipeFilterTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeFilterTagRepository
        extends JpaRepository<RecipeFilterTagEntity, RecipeFilterTagEntity.FilterTagId> {

    interface CategoryTagCount {
        String getTagValue();
        long getRecipeCount();
    }

    /**
     * Counts recipes per tag value within a category, filtered by a free-text search, ordered by
     * the numeric sort key. Generic replacement for the old GregTech voltage-tier search query.
     */
    @Query("""
            select t.tagValue as tagValue, count(t.recipeId) as recipeCount
            from RecipeFilterTagEntity t
            where t.datasetId = :datasetId
              and t.tagKey = :tagKey
              and exists (
                select 1
                from RecipeSearchDocumentEntity d
                where d.datasetId = t.datasetId
                  and d.recipeId = t.recipeId
                  and d.categoryId = :categoryId
                  and d.searchText like :pattern
              )
            group by t.tagValue
            order by min(t.sortValue) asc, t.tagValue asc
            """)
    List<CategoryTagCount> findTagValuesByCategoryAndSearch(
            @Param("datasetId") String datasetId,
            @Param("categoryId") String categoryId,
            @Param("tagKey") String tagKey,
            @Param("pattern") String pattern);
}
