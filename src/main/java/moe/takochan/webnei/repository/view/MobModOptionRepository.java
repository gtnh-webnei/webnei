package moe.takochan.webnei.repository.view;

import java.util.List;
import moe.takochan.webnei.model.entity.view.MobModOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MobModOptionRepository
        extends JpaRepository<MobModOptionEntity, MobModOptionEntity.ModOptionId> {

    List<MobModOptionEntity> findByDatasetIdOrderByNameAscModIdAsc(String datasetId);
}
