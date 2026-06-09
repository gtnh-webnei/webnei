package moe.takochan.webnei.repository.table;

import moe.takochan.webnei.model.entity.table.FluidSearchDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FluidSearchDocumentRepository
        extends JpaRepository<FluidSearchDocumentEntity, FluidSearchDocumentEntity.FluidSearchDocumentId>,
                JpaSpecificationExecutor<FluidSearchDocumentEntity> {
}
