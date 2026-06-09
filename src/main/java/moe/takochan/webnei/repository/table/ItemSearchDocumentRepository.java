package moe.takochan.webnei.repository.table;

import moe.takochan.webnei.model.entity.table.ItemSearchDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemSearchDocumentRepository
        extends JpaRepository<ItemSearchDocumentEntity, ItemSearchDocumentEntity.ItemSearchDocumentId>,
                JpaSpecificationExecutor<ItemSearchDocumentEntity> {
}
