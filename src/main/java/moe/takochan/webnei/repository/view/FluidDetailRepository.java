package moe.takochan.webnei.repository.view;

import moe.takochan.webnei.model.entity.view.FluidDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FluidDetailRepository extends JpaRepository<FluidDetailEntity, FluidDetailEntity.FluidDetailId> {
}
