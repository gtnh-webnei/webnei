package moe.takochan.webnei.recipe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeLookupBrowserRepository
        extends JpaRepository<RecipeLookupBrowserEntity, RecipeLookupBrowserEntity.LookupBrowserId>,
                JpaSpecificationExecutor<RecipeLookupBrowserEntity> {
}
