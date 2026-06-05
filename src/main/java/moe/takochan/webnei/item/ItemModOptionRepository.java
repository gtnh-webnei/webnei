package moe.takochan.webnei.item;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemModOptionRepository
        extends JpaRepository<ItemModOptionEntity, ItemModOptionEntity.ModOptionId> {

    List<ItemModOptionEntity> findByDatasetIdOrderByNameAscModIdAsc(String datasetId);

    Optional<ItemModOptionEntity> findByDatasetIdAndModId(String datasetId, String modId);
}
