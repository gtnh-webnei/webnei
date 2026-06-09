package moe.takochan.webnei.repository.view;

import moe.takochan.webnei.model.entity.view.FluidListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FluidListRepository extends JpaRepository<FluidListEntity, FluidListEntity.FluidListId>,
        JpaSpecificationExecutor<FluidListEntity> {
}
