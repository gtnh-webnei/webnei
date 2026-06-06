package moe.takochan.webnei.item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
import moe.takochan.webnei.gtore.GtBartWorksOreEntity;
import moe.takochan.webnei.gtore.GtDimensionDisplayEntity;
import moe.takochan.webnei.gtore.GtDimensionDisplayRepository;
import moe.takochan.webnei.gtore.GtDimensionRef;
import moe.takochan.webnei.gtore.GtBartWorksOreLayerRepository;
import moe.takochan.webnei.gtore.GtBartWorksOreRepository;
import moe.takochan.webnei.gtore.GtOreSmallEntity;
import moe.takochan.webnei.gtore.GtOreSmallRepository;
import moe.takochan.webnei.gtore.GtOreVeinEntity;
import moe.takochan.webnei.gtore.GtOreVeinLayerRepository;
import moe.takochan.webnei.gtore.GtOreVeinDimensionRepository;
import moe.takochan.webnei.gtore.GtOreVeinLayerVariantRepository;
import moe.takochan.webnei.gtore.GtOreVeinRepository;
import moe.takochan.webnei.gtore.GtOreSmallDimensionRepository;

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
    private final GtDimensionDisplayRepository dimensionDisplayRepo;
    private final GtOreVeinRepository oreVeinRepo;
    private final GtOreVeinLayerRepository oreVeinLayerRepo;
    private final GtOreVeinLayerVariantRepository oreVeinLayerVariantRepo;
    private final GtOreVeinDimensionRepository oreVeinDimensionRepo;
    private final GtOreSmallRepository smallOreRepo;
    private final GtOreSmallDimensionRepository smallOreDimensionRepo;
    private final GtBartWorksOreRepository bartWorksOreRepo;
    private final GtBartWorksOreLayerRepository bartWorksOreLayerRepo;

    public ItemService(DatasetService datasetService, ItemVariantRepository itemRepo,
                       ItemModOptionRepository modOptionRepo,
                       NeiPanelEntryRepository panelRepo, AssetUrlBuilder assetUrlBuilder,
                       GtDimensionDisplayRepository dimensionDisplayRepo,
                       GtOreVeinRepository oreVeinRepo,
                       GtOreVeinLayerRepository oreVeinLayerRepo,
                       GtOreVeinLayerVariantRepository oreVeinLayerVariantRepo,
                       GtOreVeinDimensionRepository oreVeinDimensionRepo,
                       GtOreSmallRepository smallOreRepo,
                       GtOreSmallDimensionRepository smallOreDimensionRepo,
                       GtBartWorksOreRepository bartWorksOreRepo,
                       GtBartWorksOreLayerRepository bartWorksOreLayerRepo) {
        this.datasetService = datasetService;
        this.itemRepo = itemRepo;
        this.modOptionRepo = modOptionRepo;
        this.panelRepo = panelRepo;
        this.assetUrlBuilder = assetUrlBuilder;
        this.dimensionDisplayRepo = dimensionDisplayRepo;
        this.oreVeinRepo = oreVeinRepo;
        this.oreVeinLayerRepo = oreVeinLayerRepo;
        this.oreVeinLayerVariantRepo = oreVeinLayerVariantRepo;
        this.oreVeinDimensionRepo = oreVeinDimensionRepo;
        this.smallOreRepo = smallOreRepo;
        this.smallOreDimensionRepo = smallOreDimensionRepo;
        this.bartWorksOreRepo = bartWorksOreRepo;
        this.bartWorksOreLayerRepo = bartWorksOreLayerRepo;
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
                worldGeneration(datasetId, itemVariantId));
    }

    private List<ItemWorldGenerationRef> worldGeneration(String datasetId, String itemVariantId) {
        Map<String, ItemWorldGenerationRef> refs = new LinkedHashMap<>();
        Sort layerSort = Sort.by("veinName").ascending().and(Sort.by("layer").ascending());
        oreVeinLayerRepo.findByDatasetIdAndBlockItemVariantId(datasetId, itemVariantId, layerSort)
                .forEach(layer -> oreVeinRepo.findById(new GtOreVeinEntity.OreVeinId(datasetId, layer.getVeinName()))
                        .ifPresent(vein -> refs.putIfAbsent("ore-veins|" + vein.getVeinName(), oreVeinRef(datasetId, vein))));
        oreVeinLayerVariantRepo.findByDatasetIdAndItemVariantId(datasetId, itemVariantId, layerSort)
                .forEach(variant -> oreVeinRepo.findById(new GtOreVeinEntity.OreVeinId(datasetId, variant.getVeinName()))
                        .ifPresent(vein -> refs.putIfAbsent("ore-veins|" + vein.getVeinName(), oreVeinRef(datasetId, vein))));

        smallOreRepo.findByDatasetIdAndSmallOreItemVariantId(datasetId, itemVariantId, Sort.by("materialName").ascending())
                .forEach(ore -> {
                    refs.putIfAbsent("small-ores|" + ore.getOreGenName(), smallOreRef(datasetId, ore));
                    oreVeinLayerRepo.findByDatasetIdAndMaterialName(datasetId, ore.getMaterialName(), layerSort)
                            .forEach(layer -> oreVeinRepo.findById(new GtOreVeinEntity.OreVeinId(datasetId, layer.getVeinName()))
                                    .ifPresent(vein -> refs.putIfAbsent("ore-veins|" + vein.getVeinName(), oreVeinRef(datasetId, vein))));
                });

        Sort bartWorksSort = Sort.by("entryType").ascending().and(Sort.by("dimensionDisplayName").ascending());
        bartWorksOreRepo.findByDatasetIdAndResultItemVariantId(datasetId, itemVariantId, bartWorksSort)
                .forEach(ore -> refs.putIfAbsent("bartworks-ores|" + ore.getEntryId(), bartWorksRef(ore)));
        bartWorksOreLayerRepo.findByDatasetIdAndItemVariantId(datasetId, itemVariantId, Sort.by("entryId").ascending())
                .forEach(layer -> bartWorksOreRepo.findById(new GtBartWorksOreEntity.BartWorksOreId(datasetId, layer.getEntryId()))
                        .ifPresent(ore -> refs.putIfAbsent("bartworks-ores|" + ore.getEntryId(), bartWorksRef(ore))));
        return List.copyOf(refs.values());
    }

    private ItemWorldGenerationRef oreVeinRef(String datasetId, GtOreVeinEntity vein) {
        return new ItemWorldGenerationRef(
                "ore-veins",
                vein.getVeinName(),
                vein.getDisplayName(),
                "矿脉信息",
                oreVeinDimensionRepo.findByDatasetIdAndVeinNameAndEnabled(
                                datasetId, vein.getVeinName(), true, Sort.by("displayAbbr").ascending())
                        .stream()
                        .map(d -> dimensionRef(datasetId, d.getDimension()))
                        .toList(),
                "Y " + vein.getHeightMin() + "-" + vein.getHeightMax() + " · 权重 " + vein.getWeight());
    }

    private ItemWorldGenerationRef smallOreRef(String datasetId, GtOreSmallEntity ore) {
        return new ItemWorldGenerationRef(
                "small-ores",
                ore.getOreGenName(),
                itemDisplayName(datasetId, ore.getSmallOreItemVariantId()),
                "贫瘠矿石信息",
                smallOreDimensionRepo.findByDatasetIdAndOreGenNameAndEnabled(
                                datasetId, ore.getOreGenName(), true, Sort.by("displayAbbr").ascending())
                        .stream()
                        .map(d -> dimensionRef(datasetId, d.getDimension()))
                        .toList(),
                "Y " + ore.getHeightMin() + "-" + ore.getHeightMax() + " · 每区块 " + ore.getAmountPerChunk());
    }

    private ItemWorldGenerationRef bartWorksRef(GtBartWorksOreEntity ore) {
        String stat = "Y " + ore.getHeightMin() + "-" + ore.getHeightMax() + " · "
                + ("small".equals(ore.getEntryType()) ? "每区块 " + ore.getAmountPerChunk() : "权重 " + ore.getWeight());
        return new ItemWorldGenerationRef(
                "bartworks-ores",
                ore.getEntryId(),
                ore.getResultDisplayName(),
                "BartWorks Ores",
                List.of(dimensionRef(ore.getDatasetId(), ore.getDimension(), ore.getDimensionDisplayName())),
                stat);
    }

    private GtDimensionRef dimensionRef(String datasetId, String dimension) {
        return dimensionRef(datasetId, dimension, dimension);
    }

    private GtDimensionRef dimensionRef(String datasetId, String dimension, String fallbackName) {
        return dimensionDisplayRepo.findById(new GtDimensionDisplayEntity.DimensionDisplayId(datasetId, dimension))
                .map(d -> new GtDimensionRef(
                        d.getDimension(), d.getFullName(), d.getDisplayName(), d.getDisplayAbbr(),
                        d.getIconItemVariantId(), itemAssetUrl(datasetId, d.getIconItemVariantId()), d.getSortOrder()))
                .orElse(new GtDimensionRef(dimension, fallbackName, fallbackName, fallbackName, "", null, Integer.MAX_VALUE));
    }

    private String itemAssetUrl(String datasetId, String itemVariantId) {
        if (itemVariantId == null || itemVariantId.isBlank()) return null;
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return itemRepo.findById(new ItemVariantBrowserEntity.ItemVariantId(datasetId, itemVariantId))
                .map(i -> assetUrlBuilder.build(dataset, i.getAssetPath(), i.getAssetSha256()))
                .orElse(null);
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

    private String itemDisplayName(String datasetId, String itemVariantId) {
        return itemRepo.findById(new ItemVariantBrowserEntity.ItemVariantId(datasetId, itemVariantId))
                .map(ItemVariantBrowserEntity::getDisplayName)
                .orElse(itemVariantId);
    }

    private String modName(String datasetId, String modId) {
        return modOptionRepo.findByDatasetIdAndModId(datasetId, modId)
                .map(ItemModOptionEntity::getName)
                .orElse(modId);
    }
}
