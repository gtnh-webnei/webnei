package moe.takochan.webnei.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemListRepository extends JpaRepository<ItemListEntity, ItemListEntity.ItemListId>,
        JpaSpecificationExecutor<ItemListEntity> {
}
