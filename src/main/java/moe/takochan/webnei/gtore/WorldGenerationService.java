package moe.takochan.webnei.gtore;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.dataset.DatasetSummary;
import moe.takochan.webnei.item.ItemVariantBrowserEntity;
import moe.takochan.webnei.item.ItemVariantRepository;
import moe.takochan.webnei.item.ItemWorldGenerationRef;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class WorldGenerationService {

    private final AssetUrlBuilder assetUrlBuilder;
    private final ItemVariantRepository itemVariantRepo;
    private final GtDimensionDisplayRepository dimensionDisplayRepo;
    private final GtOreVeinRepository oreVeinRepo;
    private final GtOreVeinLayerRepository oreVeinLayerRepo;
    private final GtOreVeinLayerVariantRepository oreVeinLayerVariantRepo;
    private final GtOreVeinDimensionRepository oreVeinDimensionRepo;
    private final GtOreSmallRepository smallOreRepo;
    private final GtOreSmallDimensionRepository smallOreDimensionRepo;
    private final GtBartWorksOreRepository bartWorksOreRepo;
    private final GtBartWorksOreLayerRepository bartWorksOreLayerRepo;

    public WorldGenerationService(AssetUrlBuilder assetUrlBuilder,
                                  ItemVariantRepository itemVariantRepo,
                                  GtDimensionDisplayRepository dimensionDisplayRepo,
                                  GtOreVeinRepository oreVeinRepo,
                                  GtOreVeinLayerRepository oreVeinLayerRepo,
                                  GtOreVeinLayerVariantRepository oreVeinLayerVariantRepo,
                                  GtOreVeinDimensionRepository oreVeinDimensionRepo,
                                  GtOreSmallRepository smallOreRepo,
                                  GtOreSmallDimensionRepository smallOreDimensionRepo,
                                  GtBartWorksOreRepository bartWorksOreRepo,
                                  GtBartWorksOreLayerRepository bartWorksOreLayerRepo) {
        this.assetUrlBuilder = assetUrlBuilder;
        this.itemVariantRepo = itemVariantRepo;
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

    public List<ItemWorldGenerationRef> forItem(DatasetSummary dataset, String itemVariantId) {
        String datasetId = dataset.datasetId();
        Map<String, ItemWorldGenerationRef> refs = new LinkedHashMap<>();
        Sort layerSort = Sort.by("veinName").ascending().and(Sort.by("layer").ascending());

        List<GtOreSmallEntity> smallOres = smallOreRepo.findByDatasetIdAndSmallOreItemVariantId(
                datasetId, itemVariantId, Sort.by("materialName").ascending());
        List<String> materialNames = smallOres.stream()
                .map(GtOreSmallEntity::getMaterialName)
                .distinct()
                .toList();
        List<GtOreVeinLayerEntity> materialLayers = materialNames.isEmpty()
                ? List.of()
                : oreVeinLayerRepo.findByDatasetIdAndMaterialNameIn(datasetId, materialNames, layerSort);
        List<GtOreVeinLayerEntity> blockLayers =
                oreVeinLayerRepo.findByDatasetIdAndBlockItemVariantId(datasetId, itemVariantId, layerSort);
        List<GtOreVeinLayerVariantEntity> layerVariants =
                oreVeinLayerVariantRepo.findByDatasetIdAndItemVariantId(datasetId, itemVariantId, layerSort);

        List<String> veinNames = java.util.stream.Stream.of(
                        blockLayers.stream().map(GtOreVeinLayerEntity::getVeinName),
                        layerVariants.stream().map(GtOreVeinLayerVariantEntity::getVeinName),
                        materialLayers.stream().map(GtOreVeinLayerEntity::getVeinName))
                .flatMap(s -> s)
                .distinct()
                .toList();
        Map<String, GtOreVeinEntity> oreVeins = loadOreVeins(datasetId, veinNames);

        Sort bartWorksSort = Sort.by("entryType").ascending().and(Sort.by("dimensionDisplayName").ascending());
        List<GtBartWorksOreEntity> directBartWorksOres = bartWorksOreRepo.findByDatasetIdAndResultItemVariantId(
                datasetId, itemVariantId, bartWorksSort);
        List<GtBartWorksOreLayerEntity> bartWorksLayers =
                bartWorksOreLayerRepo.findByDatasetIdAndItemVariantId(datasetId, itemVariantId, Sort.by("entryId").ascending());
        Map<String, GtBartWorksOreEntity> bartWorksOres = loadBartWorksOres(
                datasetId,
                bartWorksLayers.stream().map(GtBartWorksOreLayerEntity::getEntryId).distinct().toList());

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
        return itemVariantRepo.findByDatasetIdAndItemVariantIdIn(datasetId, itemVariantIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(ItemVariantBrowserEntity::getItemVariantId, item -> item));
    }

    private String itemDisplayName(String itemVariantId, Map<String, ItemVariantBrowserEntity> itemVariants) {
        ItemVariantBrowserEntity item = itemVariants.get(itemVariantId);
        return item == null ? itemVariantId : item.getDisplayName();
    }
}
