package moe.takochan.webnei.gtore;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GtUndergroundFluidBrowserRepository
        extends JpaRepository<GtUndergroundFluidBrowserEntity, GtUndergroundFluidBrowserEntity.UndergroundFluidBrowserId>,
                JpaSpecificationExecutor<GtUndergroundFluidBrowserEntity> {
}
