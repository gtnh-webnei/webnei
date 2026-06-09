package moe.takochan.webnei.repository.view;

import java.util.List;
import moe.takochan.webnei.model.entity.view.FluidModOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FluidModOptionRepository
        extends JpaRepository<FluidModOptionEntity, FluidModOptionEntity.ModOptionId> {

    List<FluidModOptionEntity> findByDatasetIdOrderByNameAscModIdAsc(String datasetId);
}
