package moe.takochan.webnei.item;

import java.util.ArrayList;
import java.util.HashMap;
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
                worldGeneration(dataset, itemVariantId));
    }

    private List<ItemWorldGenerationRef> worldGeneration(DatasetSummary dataset, String itemVariantId) {
        String datasetId = dataset.datasetId();
        Map<String, ItemWorldGenerationRef> refs = new LinkedHashMap<>();
        Sort layerSort = Sort.by("veinName").ascending().and(Sort.by("layer").ascending());

        List<GtOreSmallEntity> smallOres = smallOreRepo.findByDatasetIdAndSmallOreItemVariantId(
                datasetId, itemVariantId, Sort.by("materialName").ascending());
        List<String> materialNames = smallOres.stream()
                .map(GtOreSmallEntity::getMaterialName)
                .distinct()
                .toList();
        List<moe.takochan.webnei.gtore.GtOreVeinLayerEntity> materialLayers = materialNames.isEmpty()
                ? List.of()
                : oreVeinLayerRepo.findByDatasetIdAndMaterialNameIn(datasetId, materialNames, layerSort);
        List<moe.takochan.webnei.gtore.GtOreVeinLayerEntity> blockLayers =
                oreVeinLayerRepo.findByDatasetIdAndBlockItemVariantId(datasetId, itemVariantId, layerSort);
        List<moe.takochan.webnei.gtore.GtOreVeinLayerVariantEntity> layerVariants =
                oreVeinLayerVariantRepo.findByDatasetIdAndItemVariantId(datasetId, itemVariantId, layerSort);

        List<String> veinNames = java.util.stream.Stream.of(
                        blockLayers.stream().map(moe.takochan.webnei.gtore.GtOreVeinLayerEntity::getVeinName),
                        layerVariants.stream().map(moe.takochan.webnei.gtore.GtOreVeinLayerVariantEntity::getVeinName),
                        materialLayers.stream().map(moe.takochan.webnei.gtore.GtOreVeinLayerEntity::getVeinName))
                .flatMap(s -> s)
                .distinct()
                .toList();
        Map<String, GtOreVeinEntity> oreVeins = loadOreVeins(datasetId, veinNames);

        Sort bartWorksSort = Sort.by("entryType").ascending().and(Sort.by("dimensionDisplayName").ascending());
        List<GtBartWorksOreEntity> directBartWorksOres = bartWorksOreRepo.findByDatasetIdAndResultItemVariantId(
                datasetId, itemVariantId, bartWorksSort);
        List<moe.takochan.webnei.gtore.GtBartWorksOreLayerEntity> bartWorksLayers =
                bartWorksOreLayerRepo.findByDatasetIdAndItemVariantId(datasetId, itemVariantId, Sort.by("entryId").ascending());
        Map<String, GtBartWorksOreEntity> bartWorksOres = loadBartWorksOres(
                datasetId,
                bartWorksLayers.stream().map(moe.takochan.webnei.gtore.GtBartWorksOreLayerEntity::getEntryId).distinct().toList());

        Map<String, GtDimensionDisplayEntity> dimensions = dimensionDisplayRepo.findByDatasetId(datasetId, Sort.unsorted())
                .stream()
                .collect(java.util.stream.Collectors.toMap(GtDimensionDisplayEntity::getDimension, d -> d));
        List<String> itemVariantIds = java.util.stream.Stream.concat(
                        dimensions.values().stream().map(GtDimensionDisplayEntity::getIconItemVariantId),
                        smallOres.stream().map(GtOreSmallEntity::getSmallOreItemVariantId))
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        Map<String, ItemVariantBrowserEntity> itemVariants = loadItemVariants(datasetId, itemVariantIds);

        blockLayers.forEach(layer -> putOreVeinRef(refs, dataset, oreVeins.get(layer.getVeinName()), dimensions, itemVariants));
        layerVariants.forEach(variant -> putOreVeinRef(refs, dataset, oreVeins.get(variant.getVeinName()), dimensions, itemVariants));
        smallOres.forEach(ore -> {
            refs.putIfAbsent("small-ores|" + ore.getOreGenName(), smallOreRef(dataset, ore, dimensions, itemVariants));
            materialLayers.stream()
                    .filter(layer -> layer.getMaterialName().equals(ore.getMaterialName()))
                    .forEach(layer -> putOreVeinRef(refs, dataset, oreVeins.get(layer.getVeinName()), dimensions, itemVariants));
        });
        directBartWorksOres.forEach(ore -> refs.putIfAbsent(
                "bartworks-ores|" + ore.getEntryId(), bartWorksRef(dataset, ore, dimensions, itemVariants)));
        bartWorksLayers.forEach(layer -> {
            GtBartWorksOreEntity ore = bartWorksOres.get(layer.getEntryId());
            if (ore != null) {
                refs.putIfAbsent("bartworks-ores|" + ore.getEntryId(), bartWorksRef(dataset, ore, dimensions, itemVariants));
            }
        });
        return List.copyOf(refs.values());
    }

    private void putOreVeinRef(
            Map<String, ItemWorldGenerationRef> refs,
            DatasetSummary dataset,
            GtOreVeinEntity vein,
            Map<String, GtDimensionDisplayEntity> dimensions,
            Map<String, ItemVariantBrowserEntity> itemVariants) {
        if (vein != null) {
            refs.putIfAbsent("ore-veins|" + vein.getVeinName(), oreVeinRef(dataset, vein, dimensions, itemVariants));
        }
    }

    private ItemWorldGenerationRef oreVeinRef(
            DatasetSummary dataset,
            GtOreVeinEntity vein,
            Map<String, GtDimensionDisplayEntity> dimensions,
            Map<String, ItemVariantBrowserEntity> itemVariants) {
        String datasetId = dataset.datasetId();
        return new ItemWorldGenerationRef(
                "ore-veins",
                vein.getVeinName(),
                vein.getDisplayName(),
                "矿脉信息",
                oreVeinDimensionRepo.findByDatasetIdAndVeinNameAndEnabled(
                                datasetId, vein.getVeinName(), true, Sort.by("displayAbbr").ascending())
                        .stream()
                        .map(d -> dimensionRef(dataset, d.getDimension(), d.getDimension(), dimensions, itemVariants))
                        .toList(),
                "Y " + vein.getHeightMin() + "-" + vein.getHeightMax() + " · 权重 " + vein.getWeight());
    }

    private ItemWorldGenerationRef smallOreRef(
            DatasetSummary dataset,
            GtOreSmallEntity ore,
            Map<String, GtDimensionDisplayEntity> dimensions,
            Map<String, ItemVariantBrowserEntity> itemVariants) {
        String datasetId = dataset.datasetId();
        return new ItemWorldGenerationRef(
                "small-ores",
                ore.getOreGenName(),
                itemDisplayName(ore.getSmallOreItemVariantId(), itemVariants),
                "贫瘠矿石信息",
                smallOreDimensionRepo.findByDatasetIdAndOreGenNameAndEnabled(
                                datasetId, ore.getOreGenName(), true, Sort.by("displayAbbr").ascending())
                        .stream()
                        .map(d -> dimensionRef(dataset, d.getDimension(), d.getDimension(), dimensions, itemVariants))
                        .toList(),
                "Y " + ore.getHeightMin() + "-" + ore.getHeightMax() + " · 每区块 " + ore.getAmountPerChunk());
    }

    private ItemWorldGenerationRef bartWorksRef(
            DatasetSummary dataset,
            GtBartWorksOreEntity ore,
            Map<String, GtDimensionDisplayEntity> dimensions,
            Map<String, ItemVariantBrowserEntity> itemVariants) {
        String stat = "Y " + ore.getHeightMin() + "-" + ore.getHeightMax() + " · "
                + ("small".equals(ore.getEntryType()) ? "每区块 " + ore.getAmountPerChunk() : "权重 " + ore.getWeight());
        return new ItemWorldGenerationRef(
                "bartworks-ores",
                ore.getEntryId(),
                ore.getResultDisplayName(),
                "BartWorks Ores",
                List.of(dimensionRef(dataset, ore.getDimension(), ore.getDimensionDisplayName(), dimensions, itemVariants)),
                stat);
    }

    private GtDimensionRef dimensionRef(
            DatasetSummary dataset,
            String dimension,
            String fallbackName,
            Map<String, GtDimensionDisplayEntity> dimensions,
            Map<String, ItemVariantBrowserEntity> itemVariants) {
        GtDimensionDisplayEntity d = dimensions.get(dimension);
        if (d == null) {
            return new GtDimensionRef(dimension, fallbackName, fallbackName, fallbackName, "", null, Integer.MAX_VALUE);
        }
        return new GtDimensionRef(
                d.getDimension(), d.getFullName(), d.getDisplayName(), d.getDisplayAbbr(),
                d.getIconItemVariantId(), itemAssetUrl(dataset, d.getIconItemVariantId(), itemVariants), d.getSortOrder());
    }

    private String itemAssetUrl(
            DatasetSummary dataset,
            String itemVariantId,
            Map<String, ItemVariantBrowserEntity> itemVariants) {
        if (itemVariantId == null || itemVariantId.isBlank()) return null;
        ItemVariantBrowserEntity item = itemVariants.get(itemVariantId);
        return item == null ? null : assetUrlBuilder.build(dataset, item.getAssetPath(), item.getAssetSha256());
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

    private Map<String, GtOreVeinEntity> loadOreVeins(String datasetId, List<String> veinNames) {
        if (veinNames.isEmpty()) return Map.of();
        Map<String, GtOreVeinEntity> out = new HashMap<>();
        oreVeinRepo.findAllById(veinNames.stream()
                        .map(name -> new GtOreVeinEntity.OreVeinId(datasetId, name))
                        .toList())
                .forEach(vein -> out.put(vein.getVeinName(), vein));
        return out;
    }

    private Map<String, GtBartWorksOreEntity> loadBartWorksOres(String datasetId, List<String> entryIds) {
        if (entryIds.isEmpty()) return Map.of();
        Map<String, GtBartWorksOreEntity> out = new HashMap<>();
        bartWorksOreRepo.findAllById(entryIds.stream()
                        .map(entryId -> new GtBartWorksOreEntity.BartWorksOreId(datasetId, entryId))
                        .toList())
                .forEach(ore -> out.put(ore.getEntryId(), ore));
        return out;
    }

    private Map<String, ItemVariantBrowserEntity> loadItemVariants(String datasetId, List<String> itemVariantIds) {
        if (itemVariantIds.isEmpty()) return Map.of();
        return itemRepo.findByDatasetIdAndItemVariantIdIn(datasetId, itemVariantIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(ItemVariantBrowserEntity::getItemVariantId, item -> item));
    }

    private String itemDisplayName(String itemVariantId, Map<String, ItemVariantBrowserEntity> itemVariants) {
        ItemVariantBrowserEntity item = itemVariants.get(itemVariantId);
        return item == null ? itemVariantId : item.getDisplayName();
    }

    private String modName(String datasetId, String modId) {
        return modOptionRepo.findByDatasetIdAndModId(datasetId, modId)
                .map(ItemModOptionEntity::getName)
                .orElse(modId);
    }
}
