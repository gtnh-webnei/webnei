package moe.takochan.webnei.recipe;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeCategoryMachineBrowserRepository
        extends JpaRepository<RecipeCategoryMachineBrowserEntity, RecipeCategoryMachineBrowserEntity.MachineBrowserId> {

    List<RecipeCategoryMachineBrowserEntity> findByDatasetIdAndCategoryId(
            String datasetId, String categoryId, Sort sort);
}
