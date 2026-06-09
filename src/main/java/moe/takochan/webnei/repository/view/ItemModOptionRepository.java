package moe.takochan.webnei.repository.view;

import java.util.List;
import moe.takochan.webnei.model.entity.view.ItemModOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemModOptionRepository
        extends JpaRepository<ItemModOptionEntity, ItemModOptionEntity.ModOptionId> {

    List<ItemModOptionEntity> findByDatasetIdOrderByNameAscModIdAsc(String datasetId);
}
