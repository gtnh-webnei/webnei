package moe.takochan.webnei.repository.view;

import moe.takochan.webnei.model.entity.view.ItemDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemDetailRepository extends JpaRepository<ItemDetailEntity, ItemDetailEntity.ItemDetailId> {
}
