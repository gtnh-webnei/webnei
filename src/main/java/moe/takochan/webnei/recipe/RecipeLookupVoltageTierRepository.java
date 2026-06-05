package moe.takochan.webnei.recipe;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeLookupVoltageTierRepository
        extends JpaRepository<RecipeLookupVoltageTierEntity, RecipeLookupVoltageTierEntity.LookupVoltageTierId> {

    List<RecipeLookupVoltageTierEntity> findByDatasetIdAndCategoryIdAndTargetIdAndLookupKindOrderByMinVoltageAsc(
            String datasetId, String categoryId, String targetId, String lookupKind);
}
