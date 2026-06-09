package moe.takochan.webnei.repository.table;

import moe.takochan.webnei.model.entity.table.MobSearchDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MobSearchDocumentRepository
        extends JpaRepository<MobSearchDocumentEntity, MobSearchDocumentEntity.MobSearchDocumentId>,
                JpaSpecificationExecutor<MobSearchDocumentEntity> {
}
