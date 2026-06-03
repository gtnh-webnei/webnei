package moe.takochan.webnei.item;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.ModOptionDto;
import moe.takochan.webnei.common.NotFoundException;
import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.dataset.DatasetService;
import moe.takochan.webnei.dataset.DatasetSummary;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private final DatasetService datasetService;
    private final ItemDao itemDao;
    private final NeiPanelEntryRepository panelRepo;
    private final AssetUrlBuilder assetUrlBuilder;

    public ItemService(DatasetService datasetService, ItemDao itemDao,
                       NeiPanelEntryRepository panelRepo, AssetUrlBuilder assetUrlBuilder) {
        this.datasetService = datasetService;
        this.itemDao = itemDao;
        this.panelRepo = panelRepo;
        this.assetUrlBuilder = assetUrlBuilder;
    }

    public PageResponse<NeiPanelEntryDto> listPanel(String datasetId, ItemQuery query, PageRequest page) {
        DatasetSummary dataset = datasetService.requireById(datasetId);

        Specification<NeiPanelEntryEntity> spec = panelSpec(datasetId, query);
        int pageIndex = page != null ? page.page() : 0;
        int pageSize = page != null ? page.size() : 100;
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageIndex, pageSize, Sort.by("panelIndex").ascending());

        Page<NeiPanelEntryEntity> result = panelRepo.findAll(spec, pageable);
        List<NeiPanelEntryDto> items = result.stream()
                .map(e -> toDto(e, dataset))
                .toList();
        return new PageResponse<>(items, pageIndex, pageSize, result.getTotalElements());
    }

    public ItemDetailDto detail(String datasetId, String itemVariantId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return itemDao.findVariant(dataset, itemVariantId)
                .orElseThrow(() -> new NotFoundException("Item variant not found: " + itemVariantId));
    }

    public List<ModOptionDto> listMods(String datasetId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return itemDao.listMods(dataset);
    }

    private static Specification<NeiPanelEntryEntity> panelSpec(String datasetId, ItemQuery query) {
        return (root, cq, cb) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("datasetId"), datasetId));
            var iv = root.join("itemVariant", JoinType.INNER);

            if (query.modId() != null && !query.modId().isBlank()) {
                predicates.add(cb.equal(iv.get("modId"), query.modId()));
            }
            if (query.q() != null && !query.q().isBlank()) {
                String pattern = "%" + query.q().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(iv.get("displayName")), pattern),
                        cb.like(cb.lower(iv.get("registryName")), pattern),
                        cb.like(cb.lower(iv.get("itemId")), pattern)));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private NeiPanelEntryDto toDto(NeiPanelEntryEntity e, DatasetSummary dataset) {
        ItemVariantBrowserEntity iv = e.getItemVariant();
        return new NeiPanelEntryDto(
                e.getItemVariantId(),
                iv.getItemId(),
                iv.getModId(),
                iv.getRegistryName(),
                iv.getDamage(),
                iv.getNbtHash(),
                iv.getDisplayName(),
                assetUrlBuilder.build(dataset, iv.getAssetPath(), iv.getAssetSha256()),
                e.getPanelIndex());
    }
}
