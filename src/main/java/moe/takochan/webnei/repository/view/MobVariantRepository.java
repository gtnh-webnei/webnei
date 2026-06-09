package moe.takochan.webnei.repository.view;

import moe.takochan.webnei.model.entity.view.MobVariantBrowserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MobVariantRepository
        extends JpaRepository<MobVariantBrowserEntity, MobVariantBrowserEntity.MobVariantId>,
                JpaSpecificationExecutor<MobVariantBrowserEntity> {
}
