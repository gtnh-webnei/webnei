package moe.takochan.webnei.repository.table;

import moe.takochan.webnei.model.entity.table.GtUndergroundFluidEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GtUndergroundFluidRepository
        extends JpaRepository<GtUndergroundFluidEntity, GtUndergroundFluidEntity.UndergroundFluidId>,
                JpaSpecificationExecutor<GtUndergroundFluidEntity> {
}
