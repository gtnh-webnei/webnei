package moe.takochan.webnei.recipe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeBrowserRepository
        extends JpaRepository<RecipeBrowserEntity, RecipeBrowserEntity.RecipeBrowserId>,
                JpaSpecificationExecutor<RecipeBrowserEntity> {
}
