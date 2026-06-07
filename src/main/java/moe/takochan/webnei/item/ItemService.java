package moe.takochan.webnei.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.ModOptionDto;
import moe.takochan.webnei.common.NotFoundException;
import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.dataset.DatasetService;
import moe.takochan.webnei.dataset.DatasetSummary;
import moe.takochan.webnei.gtore.WorldGenerationService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private final DatasetService datasetService;
    private final ItemVariantRepository itemRepo;
    private final ItemModOptionRepository modOptionRepo;
    private final NeiPanelEntryRepository panelRepo;
    private final AssetUrlBuilder assetUrlBuilder;
    private final WorldGenerationService worldGenerationService;

    public ItemService(DatasetService datasetService, ItemVariantRepository itemRepo,
                       ItemModOptionRepository modOptionRepo,
                       NeiPanelEntryRepository panelRepo, AssetUrlBuilder assetUrlBuilder,
                       WorldGenerationService worldGenerationService) {
        this.datasetService = datasetService;
        this.itemRepo = itemRepo;
        this.modOptionRepo = modOptionRepo;
        this.panelRepo = panelRepo;
        this.assetUrlBuilder = assetUrlBuilder;
        this.worldGenerationService = worldGenerationService;
    }

    public PageResponse<NeiPanelEntryDto> listPanel(String datasetId, ItemQuery query, PageRequest page) {
        DatasetSummary dataset = datasetService.requireById(datasetId);

        Specification<NeiPanelEntryEntity> spec = panelSpec(datasetId, query);
        int pageIndex = page != null ? page.page() : 0;
        int pageSize = page != null ? page.size() : 100;
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageIndex, pageSize, Sort.by("panelIndex").ascending());

        Page<NeiPanelEntryEntity> result = panelRepo.findAll(spec, pageable);
        Map<String, String> modNames = modOptionRepo.findByDatasetIdOrderByNameAscModIdAsc(datasetId)
                .stream()
                .collect(java.util.stream.Collectors.toMap(ItemModOptionEntity::getModId, ItemModOptionEntity::getName));
        List<NeiPanelEntryDto> items = result.stream()
                .map(e -> toDto(e, dataset, modNames))
                .toList();
        return new PageResponse<>(items, pageIndex, pageSize, result.getTotalElements());
    }

    public ItemDetailDto detail(String datasetId, String itemVariantId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        ItemVariantBrowserEntity e = itemRepo.findById(new ItemVariantBrowserEntity.ItemVariantId(datasetId, itemVariantId))
                .orElseThrow(() -> new NotFoundException("Item variant not found: " + itemVariantId));
        return new ItemDetailDto(
                e.getItemVariantId(),
                e.getItemId(),
                e.getModId(),
                modName(datasetId, e.getModId()),
                e.getRegistryName(),
                e.getUnlocalizedName(),
                e.getMaxStackSize(),
                e.getMaxDamage(),
                e.getDamage(),
                e.getNbtHash(),
                e.getNbtText(),
                e.getChemicalExpression(),
                e.getDisplayName(),
                e.getTooltipText(),
                assetUrlBuilder.build(dataset, e.getAssetPath(), e.getAssetSha256()),
                e.getAssetWidth(),
                e.getAssetHeight(),
                worldGenerationService.forItem(dataset, itemVariantId));
    }

    public List<ModOptionDto> listMods(String datasetId) {
        datasetService.requireById(datasetId);
        return modOptionRepo.findByDatasetIdOrderByNameAscModIdAsc(datasetId)
                .stream()
                .map(e -> new ModOptionDto(e.getModId(), e.getName()))
                .toList();
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
                String pattern = "%" + query.q().trim().toLowerCase() + "%";

                Subquery<Integer> variantMatch = cq.subquery(Integer.class);
                Root<ItemVariantSearchEntity> variant = variantMatch.from(ItemVariantSearchEntity.class);
                variantMatch.select(cb.literal(1));
                variantMatch.where(
                        cb.equal(variant.get("datasetId"), root.get("datasetId")),
                        cb.equal(variant.get("itemVariantId"), root.get("itemVariantId")),
                        cb.like(variant.get("searchText"), pattern));

                Subquery<Integer> itemMatch = cq.subquery(Integer.class);
                Root<ItemVariantSearchEntity> itemVariant = itemMatch.from(ItemVariantSearchEntity.class);
                Root<ItemSearchEntity> item = itemMatch.from(ItemSearchEntity.class);
                itemMatch.select(cb.literal(1));
                itemMatch.where(
                        cb.equal(itemVariant.get("datasetId"), root.get("datasetId")),
                        cb.equal(itemVariant.get("itemVariantId"), root.get("itemVariantId")),
                        cb.equal(item.get("datasetId"), itemVariant.get("datasetId")),
                        cb.equal(item.get("itemId"), itemVariant.get("itemId")),
                        cb.like(item.get("searchText"), pattern));

                predicates.add(cb.or(cb.exists(variantMatch), cb.exists(itemMatch)));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private NeiPanelEntryDto toDto(NeiPanelEntryEntity e, DatasetSummary dataset, Map<String, String> modNames) {
        ItemVariantBrowserEntity iv = e.getItemVariant();
        return new NeiPanelEntryDto(
                e.getItemVariantId(),
                iv.getItemId(),
                iv.getModId(),
                modNames.getOrDefault(iv.getModId(), iv.getModId()),
                iv.getRegistryName(),
                iv.getDamage(),
                iv.getNbtHash(),
                iv.getDisplayName(),
                assetUrlBuilder.build(dataset, iv.getAssetPath(), iv.getAssetSha256()),
                e.getPanelIndex());
    }

    private String modName(String datasetId, String modId) {
        return modOptionRepo.findByDatasetIdAndModId(datasetId, modId)
                .map(ItemModOptionEntity::getName)
                .orElse(modId);
    }
}
