package moe.takochan.webnei.repository.table;

import java.util.List;
import moe.takochan.webnei.model.entity.table.GregTechRecipeMetadataEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GregTechRecipeMetadataRepository
        extends JpaRepository<GregTechRecipeMetadataEntity, GregTechRecipeMetadataEntity.MetadataId> {

    List<GregTechRecipeMetadataEntity> findByDatasetIdAndRecipeIdIn(String datasetId, List<String> recipeIds, Sort sort);
}
