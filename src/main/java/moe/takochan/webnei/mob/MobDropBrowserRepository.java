package moe.takochan.webnei.mob;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MobDropBrowserRepository
        extends JpaRepository<MobDropBrowserEntity, MobDropBrowserEntity.MobDropId> {

    List<MobDropBrowserEntity> findByDatasetIdAndMobVariantId(String datasetId, String mobVariantId, Sort sort);
}
