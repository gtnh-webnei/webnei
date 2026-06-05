package moe.takochan.webnei.extras;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemAspectBrowserRepository
        extends JpaRepository<ItemAspectBrowserEntity, ItemAspectBrowserEntity.ItemAspectId> {

    List<ItemAspectBrowserEntity> findByDatasetIdAndItemVariantId(String datasetId, String itemVariantId, Sort sort);
}
