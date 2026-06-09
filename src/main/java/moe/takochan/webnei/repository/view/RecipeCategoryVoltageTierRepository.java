package moe.takochan.webnei.repository.view;

import java.util.List;
import moe.takochan.webnei.model.entity.view.RecipeCategoryVoltageTierEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeCategoryVoltageTierRepository
        extends JpaRepository<RecipeCategoryVoltageTierEntity, RecipeCategoryVoltageTierEntity.VoltageTierId> {

    List<RecipeCategoryVoltageTierEntity> findByDatasetIdAndCategoryIdOrderByMinVoltageAsc(
            String datasetId, String categoryId);
}
