package moe.takochan.webnei.item;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemVariantRepository
        extends JpaRepository<ItemVariantBrowserEntity, ItemVariantBrowserEntity.ItemVariantId>,
                JpaSpecificationExecutor<ItemVariantBrowserEntity> {

    List<ItemVariantBrowserEntity> findByDatasetIdAndItemVariantIdIn(String datasetId, List<String> itemVariantIds);
}
