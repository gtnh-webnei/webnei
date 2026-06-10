package moe.takochan.webnei.repository.view;

import java.util.List;
import moe.takochan.webnei.model.entity.view.RecipeCategoryApplicableItemBrowserEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeCategoryApplicableItemBrowserRepository
        extends JpaRepository<RecipeCategoryApplicableItemBrowserEntity, RecipeCategoryApplicableItemBrowserEntity.ApplicableItemBrowserId> {

    List<RecipeCategoryApplicableItemBrowserEntity> findByDatasetIdAndCategoryId(
            String datasetId, String categoryId, Sort sort);
}
