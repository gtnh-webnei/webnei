package moe.takochan.webnei.dataset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ModRepository
        extends JpaRepository<ModEntity, ModEntity.ModId>,
                JpaSpecificationExecutor<ModEntity> {

    java.util.List<ModEntity> findByDatasetIdOrderByNameAsc(String datasetId);
}
