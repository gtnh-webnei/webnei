package moe.takochan.webnei.recipe;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeCategoryLayoutRepository
        extends JpaRepository<RecipeCategoryLayoutEntity, RecipeCategoryLayoutEntity.CategoryLayoutId> {

    List<RecipeCategoryLayoutEntity> findByDatasetIdAndCategoryIdIn(String datasetId, List<String> categoryIds);
}
