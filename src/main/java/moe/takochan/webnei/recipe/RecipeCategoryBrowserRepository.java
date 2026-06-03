package moe.takochan.webnei.recipe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeCategoryBrowserRepository
        extends JpaRepository<RecipeCategoryBrowserEntity, RecipeCategoryBrowserEntity.CategoryId>,
                JpaSpecificationExecutor<RecipeCategoryBrowserEntity> {
}
