package moe.takochan.webnei.gtore;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GtOreSmallVariantRepository
        extends JpaRepository<GtOreSmallVariantEntity, GtOreSmallVariantEntity.OreSmallVariantId> {
}
