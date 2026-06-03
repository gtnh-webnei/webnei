package moe.takochan.webnei.fluid;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FluidVariantRepository
        extends JpaRepository<FluidVariantBrowserEntity, FluidVariantBrowserEntity.FluidVariantId>,
                JpaSpecificationExecutor<FluidVariantBrowserEntity> {
}
