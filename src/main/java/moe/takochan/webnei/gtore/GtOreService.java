package moe.takochan.webnei.gtore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.NotFoundException;
import moe.takochan.webnei.dataset.DatasetService;
import moe.takochan.webnei.dataset.DatasetSummary;
import moe.takochan.webnei.fluid.FluidModOptionEntity;
import moe.takochan.webnei.fluid.FluidModOptionRepository;
import moe.takochan.webnei.fluid.FluidVariantBrowserEntity;
import moe.takochan.webnei.fluid.FluidVariantRepository;
import moe.takochan.webnei.item.ItemVariantBrowserEntity;
import moe.takochan.webnei.item.ItemVariantRepository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class GtOreService {

    private final DatasetService datasetService;
    private final AssetUrlBuilder assetUrlBuilder;
    private final ItemVariantRepository itemVariantRepository;
    private final FluidVariantRepository fluidVariantRepository;
    private final FluidModOptionRepository fluidModOptionRepository;
    private final GtDimensionDisplayRepository dimensionRepository;
    private final GtOreVeinRepository oreVeinRepository;
    private final GtOreVeinLayerRepository oreVeinLayerRepository;
    private final GtOreVeinLayerVariantRepository oreVeinLayerVariantRepository;
    private final GtOreVeinDimensionRepository oreVeinDimensionRepository;
    private final GtOreSmallRepository oreSmallRepository;
    private final GtOreSmallDropRepository oreSmallDropRepository;
    private final GtOreSmallDimensionRepository oreSmallDimensionRepository;
    private final GtUndergroundFluidBrowserRepository undergroundFluidBrowserRepository;
    private final GtBartWorksOreRepository bartWorksOreRepository;
    private final GtBartWorksOreLayerRepository bartWorksOreLayerRepository;

    public GtOreService(
            DatasetService datasetService,
            AssetUrlBuilder assetUrlBuilder,
            ItemVariantRepository itemVariantRepository,
            FluidVariantRepository fluidVariantRepository,
            FluidModOptionRepository fluidModOptionRepository,
            GtDimensionDisplayRepository dimensionRepository,
            GtOreVeinRepository oreVeinRepository,
            GtOreVeinLayerRepository oreVeinLayerRepository,
            GtOreVeinLayerVariantRepository oreVeinLayerVariantRepository,
            GtOreVeinDimensionRepository oreVeinDimensionRepository,
            GtOreSmallRepository oreSmallRepository,
            GtOreSmallDropRepository oreSmallDropRepository,
            GtOreSmallDimensionRepository oreSmallDimensionRepository,
            GtUndergroundFluidBrowserRepository undergroundFluidBrowserRepository,
            GtBartWorksOreRepository bartWorksOreRepository,
            GtBartWorksOreLayerRepository bartWorksOreLayerRepository) {
        this.datasetService = datasetService;
        this.assetUrlBuilder = assetUrlBuilder;
        this.itemVariantRepository = itemVariantRepository;
        this.fluidVariantRepository = fluidVariantRepository;
        this.fluidModOptionRepository = fluidModOptionRepository;
        this.dimensionRepository = dimensionRepository;
        this.oreVeinRepository = oreVeinRepository;
        this.oreVeinLayerRepository = oreVeinLayerRepository;
        this.oreVeinLayerVariantRepository = oreVeinLayerVariantRepository;
        this.oreVeinDimensionRepository = oreVeinDimensionRepository;
        this.oreSmallRepository = oreSmallRepository;
        this.oreSmallDropRepository = oreSmallDropRepository;
        this.oreSmallDimensionRepository = oreSmallDimensionRepository;
        this.undergroundFluidBrowserRepository = undergroundFluidBrowserRepository;
        this.bartWorksOreRepository = bartWorksOreRepository;
        this.bartWorksOreLayerRepository = bartWorksOreLayerRepository;
    }

    public GtResourceListResponse<GtOreVeinSummary> listOreVeins(String datasetId, String q, String dimension) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        Specification<GtOreVeinEntity> spec = hasDatasetId(datasetId);
        if (q != null && !q.isBlank()) spec = spec.and(veinSearch(q));
        if (dimension != null && !dimension.isBlank()) spec = spec.and(veinHasDimension(datasetId, dimension));

        List<GtOreVeinSummary> items = oreVeinRepository.findAll(spec, oreVeinSort()).stream()
                .map(v -> oreVeinSummary(dataset, v))
                .toList();
        return new GtResourceListResponse<>(items, oreVeinDimensionOptions(dataset), List.of());
    }

    public GtOreVeinDetail oreVeinDetail(String datasetId, String veinName) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        GtOreVeinEntity vein = oreVeinRepository.findById(new GtOreVeinEntity.OreVeinId(datasetId, veinName))
                .orElseThrow(() -> new NotFoundException("GT ore vein not found: " + veinName));
        List<GtOreVeinLayerEntity> layers = oreVeinLayerRepository.findByDatasetIdAndVeinName(
                datasetId, veinName, Sort.by("layer").ascending());
        Map<String, List<GtOreVeinLayerVariantEntity>> variants = groupVeinVariants(datasetId, veinName);
        return new GtOreVeinDetail(
                vein.getDisplayName(), vein.getWeight(), vein.getSize(), vein.getDensity(),
                vein.getHeightMin(), vein.getHeightMax(),
                layers.stream().map(l -> oreVeinLayer(dataset, l, variants.getOrDefault(l.getLayer(), List.of()))).toList(),
                veinDimensions(dataset, veinName));
    }

    public GtResourceListResponse<GtSmallOreSummary> listSmallOres(String datasetId, String q, String dimension) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        Specification<GtOreSmallEntity> spec = hasDatasetId(datasetId);
        if (q != null && !q.isBlank()) spec = spec.and(smallOreSearch(q));
        if (dimension != null && !dimension.isBlank()) spec = spec.and(smallOreHasDimension(datasetId, dimension));

        List<GtSmallOreSummary> items = oreSmallRepository.findAll(spec, smallOreSort()).stream()
                .map(s -> smallOreSummary(dataset, s))
                .toList();
        return new GtResourceListResponse<>(items, smallOreDimensionOptions(dataset), List.of());
    }

    public GtSmallOreDetail smallOreDetail(String datasetId, String oreGenName) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        GtOreSmallEntity ore = oreSmallRepository.findById(new GtOreSmallEntity.OreSmallId(datasetId, oreGenName))
                .orElseThrow(() -> new NotFoundException("GT small ore not found: " + oreGenName));
        List<GtItemRef> drops = oreSmallDropRepository.findByDatasetIdAndOreGenName(
                        datasetId, oreGenName, Sort.by("dropIndex").ascending())
                .stream()
                .map(d -> itemRef(dataset, d.getItemVariantId()))
                .toList();
        return new GtSmallOreDetail(
                ore.getAmountPerChunk(), ore.getHeightMin(), ore.getHeightMax(),
                itemRef(dataset, ore.getSmallOreItemVariantId()), itemRef(dataset, ore.getDustItemVariantId()),
                drops, smallOreDimensions(dataset, oreGenName));
    }

    public GtResourceListResponse<GtUndergroundFluidSummary> listUndergroundFluids(
            String datasetId, String q, String dimension) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        Specification<GtUndergroundFluidBrowserEntity> spec = undergroundFluidHasDatasetId(datasetId);
        if (dimension != null && !dimension.isBlank()) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("dimension"), dimension));
        }
        if (q != null && !q.isBlank()) {
            spec = spec.and(undergroundFluidSearch(q));
        }

        List<GtUndergroundFluidSummary> items = groupedUndergroundFluids(
                dataset,
                undergroundFluidBrowserRepository.findAll(spec, undergroundFluidSort()));
        return new GtResourceListResponse<>(items, undergroundFluidDimensionOptions(dataset), List.of());
    }

    public GtUndergroundFluidDetail undergroundFluidDetail(String datasetId, String fluidId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        List<GtUndergroundFluidBrowserEntity> rows = undergroundFluidBrowserRepository.findByDatasetIdAndFluidId(
                datasetId, fluidId, undergroundFluidSort());
        if (rows.isEmpty()) throw new NotFoundException("GT underground fluid not found: " + fluidId);
        GtUndergroundFluidSummary summary = undergroundFluidSummary(dataset, rows);
        return new GtUndergroundFluidDetail(summary.fluid(), summary.dimensions(), summary.entries());
    }

    public GtResourceListResponse<GtBartWorksOreSummary> listBartWorksOres(
            String datasetId, String q, String type, String dimension) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        Specification<GtBartWorksOreEntity> spec = hasDatasetId(datasetId);
        if (type != null && !type.isBlank()) spec = spec.and((root, cq, cb) -> cb.equal(root.get("entryType"), type));
        if (dimension != null && !dimension.isBlank()) spec = spec.and((root, cq, cb) -> cb.equal(root.get("dimension"), dimension));
        if (q != null && !q.isBlank()) spec = spec.and(bartWorksSearch(q));

        List<GtBartWorksOreSummary> items = bartWorksOreRepository.findAll(spec, bartWorksSort()).stream()
                .map(o -> bartWorksSummary(dataset, o))
                .toList();
        return new GtResourceListResponse<>(items, bartWorksDimensionOptions(dataset), bartWorksTypes(datasetId));
    }

    public GtBartWorksOreDetail bartWorksOreDetail(String datasetId, String entryId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        GtBartWorksOreEntity ore = bartWorksOreRepository.findById(new GtBartWorksOreEntity.BartWorksOreId(datasetId, entryId))
                .orElseThrow(() -> new NotFoundException("BartWorks ore not found: " + entryId));
        List<GtBartWorksOreLayer> layers = bartWorksOreLayerRepository.findByDatasetIdAndEntryId(
                        datasetId, entryId, Sort.by("layerIndex").ascending())
                .stream()
                .map(l -> new GtBartWorksOreLayer(
                        l.getLayer(), l.getLayerIndex(), l.getOreMeta(), l.isBartworksOre(), itemRef(dataset, l.getItemVariantId())))
                .toList();
        return new GtBartWorksOreDetail(
                ore.getEntryType(), ore.getDimensionDisplayName(), itemRef(dataset, ore.getResultItemVariantId()),
                ore.getHeightMin(), ore.getHeightMax(), ore.getWeight(), ore.getDensity(), ore.getSize(),
                ore.getAmountPerChunk(), layers);
    }

    private GtOreVeinSummary oreVeinSummary(DatasetSummary dataset, GtOreVeinEntity vein) {
        Optional<GtOreVeinLayerEntity> primary = oreVeinLayerRepository.findById(
                new GtOreVeinLayerEntity.OreVeinLayerId(dataset.datasetId(), vein.getVeinName(), "primary"));
        return new GtOreVeinSummary(
                vein.getVeinName(), vein.getDisplayName(),
                primary.map(l -> itemRef(dataset, l.getBlockItemVariantId())).orElse(null),
                veinDimensions(dataset, vein.getVeinName()));
    }

    private GtOreVeinLayer oreVeinLayer(
            DatasetSummary dataset, GtOreVeinLayerEntity layer, List<GtOreVeinLayerVariantEntity> variants) {
        return new GtOreVeinLayer(
                layer.getLayer(), layer.getMaterialName(), layer.getOreMeta(), itemRef(dataset, layer.getBlockItemVariantId()),
                variants.stream().map(v -> itemRef(dataset, v.getItemVariantId())).toList());
    }

    private Map<String, List<GtOreVeinLayerVariantEntity>> groupVeinVariants(String datasetId, String veinName) {
        List<GtOreVeinLayerVariantEntity> rows = oreVeinLayerVariantRepository.findByDatasetIdAndVeinName(
                datasetId, veinName, Sort.by("layer").ascending().and(Sort.by("variantIndex").ascending()));
        Map<String, List<GtOreVeinLayerVariantEntity>> grouped = new HashMap<>();
        for (GtOreVeinLayerVariantEntity row : rows) {
            grouped.computeIfAbsent(row.getLayer(), ignored -> new ArrayList<>()).add(row);
        }
        return grouped;
    }

    private GtSmallOreSummary smallOreSummary(DatasetSummary dataset, GtOreSmallEntity ore) {
        return new GtSmallOreSummary(
                ore.getOreGenName(), itemRef(dataset, ore.getSmallOreItemVariantId()),
                smallOreDimensions(dataset, ore.getOreGenName()));
    }

    private List<GtUndergroundFluidSummary> groupedUndergroundFluids(
            DatasetSummary dataset, List<GtUndergroundFluidBrowserEntity> rows) {
        Map<String, List<GtUndergroundFluidBrowserEntity>> grouped = new LinkedHashMap<>();
        for (GtUndergroundFluidBrowserEntity row : rows) {
            grouped.computeIfAbsent(row.getFluidId(), ignored -> new ArrayList<>()).add(row);
        }
        return grouped.values().stream()
                .map(group -> undergroundFluidSummary(dataset, group))
                .toList();
    }

    private GtUndergroundFluidSummary undergroundFluidSummary(
            DatasetSummary dataset, List<GtUndergroundFluidBrowserEntity> rows) {
        GtUndergroundFluidBrowserEntity first = rows.get(0);
        List<GtUndergroundFluidEntry> entries = rows.stream()
                .map(fluid -> new GtUndergroundFluidEntry(
                        fluid.getDimension(),
                        undergroundFluidDimensionRef(dataset, fluid),
                        fluid.getChance(),
                        fluid.getMinAmount(),
                        fluid.getMaxAmount()))
                .toList();
        return new GtUndergroundFluidSummary(
                first.getFluidId(),
                fluidRef(dataset, first.getFluidVariantId()),
                entries.stream().map(GtUndergroundFluidEntry::dimensionDisplay).toList(),
                entries);
    }

    private GtDimensionRef undergroundFluidDimensionRef(
            DatasetSummary dataset, GtUndergroundFluidBrowserEntity fluid) {
        return new GtDimensionRef(
                fluid.getDimension(),
                fluid.getDimensionFullName(),
                fluid.getDimensionDisplayName(),
                fluid.getDimensionDisplayAbbr(),
                fluid.getDimensionIconItemVariantId(),
                itemRef(dataset, fluid.getDimensionIconItemVariantId()).assetUrl(),
                fluid.getDimensionSortOrder() == null ? Integer.MAX_VALUE : fluid.getDimensionSortOrder());
    }

    private GtBartWorksOreSummary bartWorksSummary(DatasetSummary dataset, GtBartWorksOreEntity ore) {
        return new GtBartWorksOreSummary(
                ore.getEntryId(), ore.getEntryType(), ore.getDimension(), ore.getDimensionDisplayName(),
                itemRef(dataset, ore.getResultItemVariantId()));
    }

    private List<GtDimensionRef> veinDimensions(DatasetSummary dataset, String veinName) {
        return oreVeinDimensionRepository.findByDatasetIdAndVeinNameAndEnabled(
                        dataset.datasetId(), veinName, true, Sort.by("displayAbbr").ascending())
                .stream()
                .map(d -> dimensionRef(dataset, d.getDimension()))
                .sorted(Comparator.comparingInt(GtDimensionRef::sortOrder))
                .toList();
    }

    private List<GtDimensionRef> smallOreDimensions(DatasetSummary dataset, String oreGenName) {
        return oreSmallDimensionRepository.findByDatasetIdAndOreGenNameAndEnabled(
                        dataset.datasetId(), oreGenName, true, Sort.by("displayAbbr").ascending())
                .stream()
                .map(d -> dimensionRef(dataset, d.getDimension()))
                .sorted(Comparator.comparingInt(GtDimensionRef::sortOrder))
                .toList();
    }

    private List<GtDimensionRef> oreVeinDimensionOptions(DatasetSummary dataset) {
        List<String> dimensions = oreVeinDimensionRepository.findByDatasetIdAndEnabled(dataset.datasetId(), true).stream()
                .map(GtOreVeinDimensionEntity::getDimension)
                .distinct()
                .toList();
        return dimensionOptions(dataset, dimensions);
    }

    private List<GtDimensionRef> smallOreDimensionOptions(DatasetSummary dataset) {
        List<String> dimensions = oreSmallDimensionRepository.findByDatasetIdAndEnabled(dataset.datasetId(), true).stream()
                .map(GtOreSmallDimensionEntity::getDimension)
                .distinct()
                .toList();
        return dimensionOptions(dataset, dimensions);
    }

    private List<GtDimensionRef> undergroundFluidDimensionOptions(DatasetSummary dataset) {
        List<String> dimensions = undergroundFluidBrowserRepository.findAll(
                        undergroundFluidHasDatasetId(dataset.datasetId()), undergroundFluidSort())
                .stream()
                .map(GtUndergroundFluidBrowserEntity::getDimension)
                .distinct()
                .toList();
        return dimensionOptions(dataset, dimensions);
    }

    private List<GtDimensionRef> bartWorksDimensionOptions(DatasetSummary dataset) {
        Map<String, GtDimensionRef> options = new HashMap<>();
        for (GtBartWorksOreEntity ore : bartWorksOreRepository.findAll(hasDatasetId(dataset.datasetId()), bartWorksSort())) {
            GtDimensionRef mapped = dimensionRef(dataset, ore.getDimension());
            if (mapped.iconAssetUrl() == null && mapped.sortOrder() == Integer.MAX_VALUE) {
                mapped = new GtDimensionRef(
                        ore.getDimension(),
                        ore.getDimensionDisplayName(),
                        ore.getDimensionDisplayName(),
                        ore.getDimensionDisplayName(),
                        "",
                        null,
                        Integer.MAX_VALUE);
            }
            options.putIfAbsent(ore.getDimension(), mapped);
        }
        return options.values().stream()
                .sorted(Comparator.comparingInt(GtDimensionRef::sortOrder).thenComparing(GtDimensionRef::displayName))
                .toList();
    }

    private List<String> bartWorksTypes(String datasetId) {
        return bartWorksOreRepository.findAll(hasDatasetId(datasetId), bartWorksSort()).stream()
                .map(GtBartWorksOreEntity::getEntryType)
                .distinct()
                .sorted()
                .toList();
    }

    private List<GtDimensionRef> dimensionOptions(DatasetSummary dataset, List<String> dimensions) {
        return dimensions.stream()
                .map(dimension -> dimensionRef(dataset, dimension))
                .sorted(Comparator.comparingInt(GtDimensionRef::sortOrder).thenComparing(GtDimensionRef::displayName))
                .toList();
    }

    private static Sort oreVeinSort() {
        return Sort.by("displayName").ascending().and(Sort.by("veinName").ascending());
    }

    private static Sort smallOreSort() {
        return Sort.by("materialName").ascending().and(Sort.by("oreGenName").ascending());
    }

    private static Sort undergroundFluidSort() {
        return Sort.by("fluidDisplayName").ascending()
                .and(Sort.by("fluidId").ascending())
                .and(Sort.by("dimensionSortOrder").ascending())
                .and(Sort.by("dimension").ascending());
    }

    private static Sort bartWorksSort() {
        return Sort.by("entryType").ascending()
                .and(Sort.by("dimensionDisplayName").ascending())
                .and(Sort.by("resultDisplayName").ascending())
                .and(Sort.by("entryId").ascending());
    }

    private GtDimensionRef dimensionRef(DatasetSummary dataset, String dimension) {
        return dimensionRepository.findById(new GtDimensionDisplayEntity.DimensionDisplayId(dataset.datasetId(), dimension))
                .map(d -> new GtDimensionRef(
                        d.getDimension(), d.getFullName(), d.getDisplayName(), d.getDisplayAbbr(),
                        d.getIconItemVariantId(), itemRef(dataset, d.getIconItemVariantId()).assetUrl(), d.getSortOrder()))
                .orElse(new GtDimensionRef(dimension, dimension, dimension, dimension, "", null, Integer.MAX_VALUE));
    }

    private GtItemRef itemRef(DatasetSummary dataset, String itemVariantId) {
        if (itemVariantId == null || itemVariantId.isBlank()) {
            return new GtItemRef("", "", null, null);
        }
        return itemVariantRepository.findById(new ItemVariantBrowserEntity.ItemVariantId(dataset.datasetId(), itemVariantId))
                .map(i -> new GtItemRef(
                        i.getItemVariantId(), i.getDisplayName(), i.getTooltipText(),
                        assetUrlBuilder.build(dataset, i.getAssetPath(), i.getAssetSha256())))
                .orElse(new GtItemRef(itemVariantId, itemVariantId, null, null));
    }

    private GtFluidRef fluidRef(DatasetSummary dataset, String fluidVariantId) {
        if (fluidVariantId == null || fluidVariantId.isBlank()) {
            return new GtFluidRef("", "", null, null, "", null, null, null);
        }
        return fluidVariantRepository.findById(new FluidVariantBrowserEntity.FluidVariantId(dataset.datasetId(), fluidVariantId))
                .map(f -> new GtFluidRef(
                        f.getFluidVariantId(),
                        f.getFluidId(),
                        f.getModId(),
                        fluidModOptionRepository.findByDatasetIdAndModId(dataset.datasetId(), f.getModId())
                                .map(FluidModOptionEntity::getName)
                                .orElse(f.getModId()),
                        f.getDisplayName(),
                        f.isGaseous(),
                        f.getTemperature(),
                        assetUrlBuilder.build(dataset, f.getAssetPath(), null)))
                .orElse(new GtFluidRef(fluidVariantId, "", null, null, fluidVariantId, null, null, null));
    }

    private static <T> Specification<T> hasDatasetId(String datasetId) {
        return (root, query, cb) -> cb.equal(root.get("datasetId"), datasetId);
    }

    private static Specification<GtOreVeinEntity> veinSearch(String q) {
        String pattern = "%" + q.toLowerCase() + "%";
        return (root, cq, cb) -> cb.like(cb.lower(root.get("displayName")), pattern);
    }

    private static Specification<GtOreVeinEntity> veinHasDimension(String datasetId, String dimension) {
        return (root, cq, cb) -> {
            var sub = cq.subquery(String.class);
            var dim = sub.from(GtOreVeinDimensionEntity.class);
            sub.select(dim.get("veinName"));
            sub.where(
                    cb.equal(dim.get("datasetId"), datasetId),
                    cb.equal(dim.get("dimension"), dimension),
                    cb.isTrue(dim.get("enabled")),
                    cb.equal(dim.get("veinName"), root.get("veinName")));
            return cb.exists(sub);
        };
    }

    private static Specification<GtOreSmallEntity> smallOreSearch(String q) {
        String pattern = "%" + q.toLowerCase() + "%";
        return (root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("materialName")), pattern),
                cb.like(cb.lower(root.get("oreGenName")), pattern));
    }

    private static Specification<GtOreSmallEntity> smallOreHasDimension(String datasetId, String dimension) {
        return (root, cq, cb) -> {
            var sub = cq.subquery(String.class);
            var dim = sub.from(GtOreSmallDimensionEntity.class);
            sub.select(dim.get("oreGenName"));
            sub.where(
                    cb.equal(dim.get("datasetId"), datasetId),
                    cb.equal(dim.get("dimension"), dimension),
                    cb.isTrue(dim.get("enabled")),
                    cb.equal(dim.get("oreGenName"), root.get("oreGenName")));
            return cb.exists(sub);
        };
    }

    private static Specification<GtUndergroundFluidBrowserEntity> undergroundFluidHasDatasetId(String datasetId) {
        return (root, query, cb) -> cb.equal(root.get("datasetId"), datasetId);
    }

    private static Specification<GtUndergroundFluidBrowserEntity> undergroundFluidSearch(String q) {
        String pattern = "%" + q.toLowerCase() + "%";
        return (root, cq, cb) -> cb.like(cb.lower(root.get("fluidDisplayName")), pattern);
    }

    private static Specification<GtBartWorksOreEntity> bartWorksSearch(String q) {
        String pattern = "%" + q.toLowerCase() + "%";
        return (root, cq, cb) -> cb.like(cb.lower(root.get("resultDisplayName")), pattern);
    }
}
