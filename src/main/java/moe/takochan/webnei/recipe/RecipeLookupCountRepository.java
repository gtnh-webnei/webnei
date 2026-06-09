package moe.takochan.webnei.recipe;

import java.util.List;

import moe.takochan.webnei.recipe.dto.LookupKindCountDto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeLookupCountRepository
        extends JpaRepository<RecipeLookupIndexEntity, RecipeLookupIndexEntity.RecipeLookupIndexId> {

    @Query("""
            select new moe.takochan.webnei.recipe.dto.LookupKindCountDto(e.lookupKind, count(distinct e.recipeId))
            from RecipeLookupIndexEntity e
            where e.datasetId = :datasetId
              and e.targetId = :targetId
            group by e.lookupKind
            """)
    List<LookupKindCountDto> countByDatasetIdAndTargetId(
            @Param("datasetId") String datasetId,
            @Param("targetId") String targetId);
}
