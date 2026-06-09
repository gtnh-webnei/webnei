package moe.takochan.webnei.service;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.NotFoundException;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.model.dto.DatasetSummary;
import moe.takochan.webnei.model.dto.ItemDetailDto;
import moe.takochan.webnei.model.dto.ItemExtras;
import moe.takochan.webnei.model.dto.ModOptionDto;
import moe.takochan.webnei.model.dto.NeiPanelEntryDto;
import moe.takochan.webnei.model.dto.PageRequest;
import moe.takochan.webnei.model.entity.table.ItemSearchDocumentEntity;
import moe.takochan.webnei.model.entity.view.ItemDetailEntity;
import moe.takochan.webnei.model.entity.view.ItemListEntity;
import moe.takochan.webnei.model.query.ItemQuery;
import moe.takochan.webnei.repository.table.ItemSearchDocumentRepository;
import moe.takochan.webnei.repository.view.ItemDetailRepository;
import moe.takochan.webnei.repository.view.ItemListRepository;
import moe.takochan.webnei.repository.view.ItemModOptionRepository;
import moe.takochan.webnei.service.ExtrasService;
import moe.takochan.webnei.service.WorldGenerationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private final ItemListRepository itemListRepo;
    private final ItemSearchDocumentRepository searchRepo;
    private final ItemDetailRepository itemDetailRepo;
    private final ItemModOptionRepository modOptionRepo;
    private final AssetUrlBuilder assetUrlBuilder;
    private final WorldGenerationService worldGenerationService;
    private final ExtrasService extrasService;

    public ItemService(ItemListRepository itemListRepo,
                       ItemSearchDocumentRepository searchRepo,
                       ItemDetailRepository itemDetailRepo,
                       ItemModOptionRepository modOptionRepo,
                       AssetUrlBuilder assetUrlBuilder,
                       WorldGenerationService worldGenerationService,
                       ExtrasService extrasService) {
        this.itemListRepo = itemListRepo;
        this.searchRepo = searchRepo;
        this.itemDetailRepo = itemDetailRepo;
        this.modOptionRepo = modOptionRepo;
        this.assetUrlBuilder = assetUrlBuilder;
        this.worldGenerationService = worldGenerationService;
        this.extrasService = extrasService;
    }

    public PageResponse<NeiPanelEntryDto> listPanel(DatasetSummary dataset, ItemQuery query, PageRequest page) {
        if (query.q() != null && !query.q().isBlank()) {
            return searchPanel(dataset, query, page);
        }

        String datasetId = dataset.datasetId();
        Specification<ItemListEntity> spec = panelSpec(datasetId, query);
        int pageIndex = page != null ? page.page() : 0;
        int pageSize = page != null ? page.size() : 100;
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageIndex, pageSize, Sort.by("panelIndex").ascending());

        Page<ItemListEntity> result = itemListRepo.findAll(spec, pageable);
        List<NeiPanelEntryDto> items = result.stream()
                .map(e -> toDto(e, dataset))
                .toList();
        return new PageResponse<>(items, pageIndex, pageSize, result.getTotalElements());
    }

    private PageResponse<NeiPanelEntryDto> searchPanel(DatasetSummary dataset, ItemQuery query, PageRequest page) {
        String datasetId = dataset.datasetId();
        int pageIndex = page != null ? page.page() : 0;
        int pageSize = page != null ? page.size() : 100;
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageIndex, pageSize, Sort.by("panelIndex").ascending());
        Page<ItemSearchDocumentEntity> result = searchRepo.findAll(searchSpec(datasetId, query), pageable);
        List<String> ids = result.stream()
                .map(ItemSearchDocumentEntity::getItemVariantId)
                .toList();
        Map<String, ItemListEntity> rows = itemListRepo.findAllById(ids.stream()
                        .map(id -> new ItemListEntity.ItemListId(datasetId, id))
                        .toList())
                .stream()
                .collect(Collectors.toMap(ItemListEntity::getItemVariantId, Function.identity()));
        List<NeiPanelEntryDto> items = ids.stream()
                .map(rows::get)
                .filter(e -> e != null)
                .map(e -> toDto(e, dataset))
                .toList();
        return new PageResponse<>(items, pageIndex, pageSize, result.getTotalElements());
    }

    public ItemDetailDto detail(DatasetSummary dataset, String itemVariantId) {
        String datasetId = dataset.datasetId();
        ItemDetailEntity e = itemDetailRepo.findById(new ItemDetailEntity.ItemDetailId(datasetId, itemVariantId))
                .orElseThrow(() -> new NotFoundException("Item variant not found: " + itemVariantId));
        ItemExtras extras = extrasService.itemExtras(dataset, itemVariantId);
        return new ItemDetailDto(
                e.getItemId(),
                e.getModId(),
                e.getModName(),
                e.getRegistryName(),
                e.getUnlocalizedName(),
                e.getMaxStackSize(),
                e.getMaxDamage(),
                e.getDamage(),
                e.getNbtText(),
                e.getChemicalExpression(),
                e.getDisplayName(),
                e.getTooltipText(),
                worldGenerationService.forItem(dataset, itemVariantId),
                extras.oreDictNames(),
                extras.relatedFluids(),
                extras.aspects());
    }

    public List<ModOptionDto> listMods(DatasetSummary dataset) {
        return modOptionRepo.findByDatasetIdOrderByNameAscModIdAsc(dataset.datasetId())
                .stream()
                .map(e -> new ModOptionDto(e.getModId(), e.getName()))
                .toList();
    }

    private static Specification<ItemListEntity> panelSpec(String datasetId, ItemQuery query) {
        return (root, cq, cb) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("datasetId"), datasetId));

            if (query.modId() != null && !query.modId().isBlank()) {
                predicates.add(cb.equal(root.get("modId"), query.modId()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static Specification<ItemSearchDocumentEntity> searchSpec(String datasetId, ItemQuery query) {
        return (root, cq, cb) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("datasetId"), datasetId));
            predicates.add(cb.like(root.get("searchText"), "%" + query.q().trim().toLowerCase() + "%"));
            if (query.modId() != null && !query.modId().isBlank()) {
                predicates.add(cb.equal(root.get("modId"), query.modId()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private NeiPanelEntryDto toDto(ItemListEntity e, DatasetSummary dataset) {
        return new NeiPanelEntryDto(
                e.getItemVariantId(),
                e.getItemId(),
                e.getModId(),
                e.getModName(),
                e.getRegistryName(),
                e.getDamage(),
                e.getDisplayName(),
                assetUrlBuilder.build(dataset, e.getAssetPath(), e.getAssetSha256()),
                e.getPanelIndex());
    }
}
