package moe.takochan.webnei.repository.view;

import java.util.List;
import moe.takochan.webnei.model.entity.view.RecipeCategoryLayoutEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeCategoryLayoutRepository
        extends JpaRepository<RecipeCategoryLayoutEntity, RecipeCategoryLayoutEntity.CategoryLayoutId> {

    List<RecipeCategoryLayoutEntity> findByDatasetIdAndCategoryIdIn(String datasetId, List<String> categoryIds);
}
