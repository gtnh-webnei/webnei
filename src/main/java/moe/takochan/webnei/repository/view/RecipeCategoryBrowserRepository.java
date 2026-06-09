package moe.takochan.webnei.repository.view;

import java.util.List;
import moe.takochan.webnei.model.entity.view.RecipeCategoryBrowserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeCategoryBrowserRepository
        extends JpaRepository<RecipeCategoryBrowserEntity, RecipeCategoryBrowserEntity.CategoryId>,
                JpaSpecificationExecutor<RecipeCategoryBrowserEntity> {

    interface ModOptionProjection {
        String getModId();
        String getName();
    }

    @Query("""
            select distinct c.modId as modId,
                   case when c.modName is null or c.modName = '' then c.modId else c.modName end as name
            from RecipeCategoryBrowserEntity c
            where c.datasetId = :datasetId
              and c.modId is not null
              and c.modId <> ''
            order by name asc, c.modId asc
            """)
    List<ModOptionProjection> findModOptions(@Param("datasetId") String datasetId);
}
