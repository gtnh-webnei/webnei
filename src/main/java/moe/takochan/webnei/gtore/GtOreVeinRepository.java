package moe.takochan.webnei.gtore;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GtOreVeinRepository
        extends JpaRepository<GtOreVeinEntity, GtOreVeinEntity.OreVeinId>,
                JpaSpecificationExecutor<GtOreVeinEntity> {
}
