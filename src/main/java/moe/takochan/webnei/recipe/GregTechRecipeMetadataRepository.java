package moe.takochan.webnei.recipe;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GregTechRecipeMetadataRepository
        extends JpaRepository<GregTechRecipeMetadataEntity, GregTechRecipeMetadataEntity.MetadataId> {

    List<GregTechRecipeMetadataEntity> findByDatasetIdAndRecipeIdIn(String datasetId, List<String> recipeIds, Sort sort);
}
