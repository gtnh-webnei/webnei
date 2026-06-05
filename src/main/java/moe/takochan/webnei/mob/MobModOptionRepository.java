package moe.takochan.webnei.mob;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MobModOptionRepository
        extends JpaRepository<MobModOptionEntity, MobModOptionEntity.ModOptionId> {

    List<MobModOptionEntity> findByDatasetIdOrderByNameAscModIdAsc(String datasetId);
}
