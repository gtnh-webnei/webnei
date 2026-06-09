package moe.takochan.webnei.repository.table;

import java.util.List;
import moe.takochan.webnei.model.entity.table.IngredientEntryEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredientEntryRepository
        extends JpaRepository<IngredientEntryEntity, IngredientEntryEntity.IngredientEntryId> {

    List<IngredientEntryEntity> findByDatasetIdAndGroupIdIn(String datasetId, List<String> groupIds, Sort sort);
}
