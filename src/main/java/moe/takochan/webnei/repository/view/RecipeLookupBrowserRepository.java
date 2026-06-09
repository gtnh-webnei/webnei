package moe.takochan.webnei.repository.view;

import moe.takochan.webnei.model.entity.view.RecipeLookupBrowserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeLookupBrowserRepository
        extends JpaRepository<RecipeLookupBrowserEntity, RecipeLookupBrowserEntity.LookupBrowserId>,
                JpaSpecificationExecutor<RecipeLookupBrowserEntity> {
}
