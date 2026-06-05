package moe.takochan.webnei.recipe;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeCategoryVoltageTierRepository
        extends JpaRepository<RecipeCategoryVoltageTierEntity, RecipeCategoryVoltageTierEntity.VoltageTierId> {

    List<RecipeCategoryVoltageTierEntity> findByDatasetIdAndCategoryIdOrderByMinVoltageAsc(
            String datasetId, String categoryId);
}
