package moe.takochan.webnei.fluid;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FluidSearchDocumentRepository
        extends JpaRepository<FluidSearchDocumentEntity, FluidSearchDocumentEntity.FluidSearchDocumentId>,
                JpaSpecificationExecutor<FluidSearchDocumentEntity> {
}
