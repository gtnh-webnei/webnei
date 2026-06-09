package moe.takochan.webnei.repository.view;

import moe.takochan.webnei.model.entity.view.RecipeBrowserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeBrowserRepository
        extends JpaRepository<RecipeBrowserEntity, RecipeBrowserEntity.RecipeBrowserId>,
                JpaSpecificationExecutor<RecipeBrowserEntity> {
}
