package moe.takochan.webnei.gtore;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GtDimensionDisplayRepository
        extends JpaRepository<GtDimensionDisplayEntity, GtDimensionDisplayEntity.DimensionDisplayId> {
    List<GtDimensionDisplayEntity> findByDatasetId(String datasetId, Sort sort);
}
