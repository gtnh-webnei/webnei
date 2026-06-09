package moe.takochan.webnei.fluid;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FluidDetailRepository extends JpaRepository<FluidDetailEntity, FluidDetailEntity.FluidDetailId> {
}
