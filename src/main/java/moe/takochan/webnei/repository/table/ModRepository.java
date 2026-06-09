package moe.takochan.webnei.repository.table;

import moe.takochan.webnei.model.entity.table.ModEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ModRepository
        extends JpaRepository<ModEntity, ModEntity.ModId>,
                JpaSpecificationExecutor<ModEntity> {
}
