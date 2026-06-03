package moe.takochan.webnei.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemVariantRepository
        extends JpaRepository<ItemVariantBrowserEntity, ItemVariantBrowserEntity.ItemVariantId>,
                JpaSpecificationExecutor<ItemVariantBrowserEntity> {
}
