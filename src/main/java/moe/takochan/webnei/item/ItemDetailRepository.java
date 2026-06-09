package moe.takochan.webnei.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemDetailRepository extends JpaRepository<ItemDetailEntity, ItemDetailEntity.ItemDetailId> {
}
