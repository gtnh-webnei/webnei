package moe.takochan.webnei.recipe;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeLookupBreakdownRepository
        extends JpaRepository<RecipeLookupBreakdownEntity, RecipeLookupBreakdownEntity.LookupBreakdownId> {

    List<RecipeLookupBreakdownEntity> findByDatasetIdAndTargetIdAndLookupKindOrderByDisplayOrderAscCategoryIdAsc(
            String datasetId, String targetId, String lookupKind);
}
