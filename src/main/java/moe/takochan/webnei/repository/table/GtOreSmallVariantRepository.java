package moe.takochan.webnei.repository.table;

import moe.takochan.webnei.model.entity.table.GtOreSmallVariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GtOreSmallVariantRepository
        extends JpaRepository<GtOreSmallVariantEntity, GtOreSmallVariantEntity.OreSmallVariantId> {
}
