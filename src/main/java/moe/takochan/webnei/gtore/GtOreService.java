package moe.takochan.webnei.gtore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.criteria.Predicate;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.NotFoundException;
import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.dataset.DatasetService;
import moe.takochan.webnei.dataset.DatasetSummary;
import moe.takochan.webnei.fluid.FluidVariantBrowserEntity;
import moe.takochan.webnei.fluid.FluidVariantRepository;
import moe.takochan.webnei.item.ItemVariantBrowserEntity;
import moe.takochan.webnei.item.ItemVariantRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class GtOreService {

    private final DatasetService datasetService;
    private final AssetUrlBuilder assetUrlBuilder;
    private final ItemVariantRepository itemVariantRepository;
    private final FluidVariantRepository fluidVariantRepository;
    private final GtDimensionDisplayRepository dimensionRepository;
    private final GtOreVeinRepository oreVeinRepository;
    private final GtOreVeinLayerRepository oreVeinLayerRepository;
    private final GtOreVeinLayerVariantRepository oreVeinLayerVariantRepository;
    private final GtOreVeinDimensionRepository oreVeinDimensionRepository;
    private final GtOreSmallRepository oreSmallRepository;
    private final GtOreSmallVariantRepository oreSmallVariantRepository;
    private final GtOreSmallDropRepository oreSmallDropRepository;
    private final GtOreSmallDimensionRepository oreSmallDimensionRepository;
    private final GtUndergroundFluidRepository undergroundFluidRepository;
    private final GtUndergroundFluidBrowserRepository undergroundFluidBrowserRepository;
    private final GtBartWorksOreRepository bartWorksOreRepository;
    private final GtBartWorksOreLayerRepository bartWorksOreLayerRepository;

    public GtOreService(
            DatasetService datasetService,
            AssetUrlBuilder assetUrlBuilder,
            ItemVariantRepository itemVariantRepository,
            FluidVariantRepository fluidVariantRepository,
            GtDimensionDisplayRepository dimensionRepository,
            GtOreVeinRepository oreVeinRepository,
            GtOreVeinLayerRepository oreVeinLayerRepository,
            GtOreVeinLayerVariantRepository oreVeinLayerVariantRepository,
            GtOreVeinDimensionRepository oreVeinDimensionRepository,
            GtOreSmallRepository oreSmallRepository,
            GtOreSmallVariantRepository oreSmallVariantRepository,
            GtOreSmallDropRepository oreSmallDropRepository,
            GtOreSmallDimensionRepository oreSmallDimensionRepository,
            GtUndergroundFluidRepository undergroundFluidRepository,
            GtUndergroundFluidBrowserRepository undergroundFluidBrowserRepository,
            GtBartWorksOreRepository bartWorksOreRepository,
            GtBartWorksOreLayerRepository bartWorksOreLayerRepository) {
        this.datasetService = datasetService;
        this.assetUrlBuilder = assetUrlBuilder;
        this.itemVariantRepository = itemVariantRepository;
        this.fluidVariantRepository = fluidVariantRepository;
        this.dimensionRepository = dimensionRepository;
        this.oreVeinRepository = oreVeinRepository;
        this.oreVeinLayerRepository = oreVeinLayerRepository;
        this.oreVeinLayerVariantRepository = oreVeinLayerVariantRepository;
        this.oreVeinDimensionRepository = oreVeinDimensionRepository;
        this.oreSmallRepository = oreSmallRepository;
        this.oreSmallVariantRepository = oreSmallVariantRepository;
        this.oreSmallDropRepository = oreSmallDropRepository;
        this.oreSmallDimensionRepository = oreSmallDimensionRepository;
        this.undergroundFluidRepository = undergroundFluidRepository;
        this.undergroundFluidBrowserRepository = undergroundFluidBrowserRepository;
        this.bartWorksOreRepository = bartWorksOreRepository;
        this.bartWorksOreLayerRepository = bartWorksOreLayerRepository;
    }

    public PageResponse<GtOreVeinSummary> listOreVeins(
            String datasetId, String q, String dimension, String materialName, PageRequest page) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        Specification<GtOreVeinEntity> spec = hasDatasetId(datasetId);
        if (q != null && !q.isBlank()) spec = spec.and(veinSearch(q));
        if (dimension != null && !dimension.isBlank()) spec = spec.and(veinHasDimension(datasetId, dimension));
        if (materialName != null && !materialName.isBlank()) spec = spec.and(veinHasMaterial(datasetId, materialName));

        Page<GtOreVeinEntity> result = oreVeinRepository.findAll(
                spec,
                pageable(page, 50, Sort.by("displayName").ascending().and(Sort.by("veinName").ascending())));
        List<GtOreVeinSummary> items = result.stream().map(v -> oreVeinSummary(dataset, v)).toList();
        return new PageResponse<>(items, pageIndex(page), pageSize(page, 50), result.getTotalElements());
    }

    public GtOreVeinDetail oreVeinDetail(String datasetId, String veinName) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        GtOreVeinEntity vein = oreVeinRepository.findById(new GtOreVeinEntity.OreVeinId(datasetId, veinName))
                .orElseThrow(() -> new NotFoundException("GT ore vein not found: " + veinName));
        List<GtOreVeinLayerEntity> layers = oreVeinLayerRepository.findByDatasetIdAndVeinName(
                datasetId, veinName, Sort.by("layer").ascending());
        Map<String, List<GtOreVeinLayerVariantEntity>> variants = groupVeinVariants(datasetId, veinName);
        return new GtOreVeinDetail(
                vein.getVeinName(), vein.getDisplayName(), vein.getWeight(), vein.getSize(), vein.getDensity(),
                vein.getHeightMin(), vein.getHeightMax(),
                layers.stream().map(l -> oreVeinLayer(dataset, l, variants.getOrDefault(l.getLayer(), List.of()))).toList(),
                veinDimensions(dataset, veinName));
    }

    public PageResponse<GtSmallOreSummary> listSmallOres(
            String datasetId, String q, String dimension, PageRequest page) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        Specification<GtOreSmallEntity> spec = hasDatasetId(datasetId);
        if (q != null && !q.isBlank()) spec = spec.and(smallOreSearch(q));
        if (dimension != null && !dimension.isBlank()) spec = spec.and(smallOreHasDimension(datasetId, dimension));

        Page<GtOreSmallEntity> result = oreSmallRepository.findAll(
                spec,
                pageable(page, 50, Sort.by("materialName").ascending().and(Sort.by("oreGenName").ascending())));
        List<GtSmallOreSummary> items = result.stream().map(s -> smallOreSummary(dataset, s)).toList();
        return new PageResponse<>(items, pageIndex(page), pageSize(page, 50), result.getTotalElements());
    }

    public GtSmallOreDetail smallOreDetail(String datasetId, String oreGenName) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        GtOreSmallEntity ore = oreSmallRepository.findById(new GtOreSmallEntity.OreSmallId(datasetId, oreGenName))
                .orElseThrow(() -> new NotFoundException("GT small ore not found: " + oreGenName));
        List<GtSmallOreVariant> variants = oreSmallVariantRepository.findByDatasetIdAndOreGenName(
                        datasetId, oreGenName, Sort.by("variantIndex").ascending())
                .stream()
                .map(v -> new GtSmallOreVariant(
                        v.getVariantIndex(),
                        itemRef(dataset, v.getSmallOreItemVariantId()),
                        itemRef(dataset, v.getDustItemVariantId())))
                .toList();
        List<GtItemRef> drops = oreSmallDropRepository.findByDatasetIdAndOreGenName(
                        datasetId, oreGenName, Sort.by("dropIndex").ascending())
                .stream()
                .map(d -> itemRef(dataset, d.getItemVariantId()))
                .toList();
        return new GtSmallOreDetail(
                ore.getOreGenName(), ore.getOreMeta(), ore.getMaterialName(), ore.getAmountPerChunk(),
                ore.getHeightMin(), ore.getHeightMax(), itemRef(dataset, ore.getSmallOreItemVariantId()),
                itemRef(dataset, ore.getDustItemVariantId()), variants, drops, smallOreDimensions(dataset, oreGenName));
    }

    public PageResponse<GtUndergroundFluidSummary> listUndergroundFluids(
            String datasetId, String q, String dimension, PageRequest page) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        Specification<GtUndergroundFluidBrowserEntity> spec = undergroundFluidHasDatasetId(datasetId);
        if (dimension != null && !dimension.isBlank()) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("dimension"), dimension));
        }
        if (q != null && !q.isBlank()) {
            spec = spec.and(undergroundFluidSearch(q));
        }

        Page<GtUndergroundFluidBrowserEntity> result = undergroundFluidBrowserRepository.findAll(
                spec,
                pageable(page, 50, Sort.by("dimensionSortOrder").ascending()
                        .and(Sort.by("fluidDisplayName").ascending())
                        .and(Sort.by("fluidId").ascending())));
        List<GtUndergroundFluidSummary> items = result.stream().map(f -> undergroundFluidSummary(dataset, f)).toList();
        return new PageResponse<>(items, pageIndex(page), pageSize(page, 50), result.getTotalElements());
    }

    public GtUndergroundFluidDetail undergroundFluidDetail(String datasetId, String fluidId, String dimension) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        GtUndergroundFluidEntity fluid = undergroundFluidRepository.findById(
                        new GtUndergroundFluidEntity.UndergroundFluidId(datasetId, fluidId, dimension))
                .orElseThrow(() -> new NotFoundException("GT underground fluid not found: " + fluidId + " " + dimension));
        return new GtUndergroundFluidDetail(
                fluid.getFluidId(), fluid.getDimension(), fluidRef(dataset, fluid.getFluidVariantId()),
                dimensionRef(dataset, fluid.getDimension()), fluid.getChance(), fluid.getMinAmount(), fluid.getMaxAmount());
    }

    public PageResponse<GtBartWorksOreSummary> listBartWorksOres(
            String datasetId, String q, String type, String dimension, PageRequest page) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        Specification<GtBartWorksOreEntity> spec = hasDatasetId(datasetId);
        if (type != null && !type.isBlank()) spec = spec.and((root, cq, cb) -> cb.equal(root.get("entryType"), type));
        if (dimension != null && !dimension.isBlank()) spec = spec.and((root, cq, cb) -> cb.equal(root.get("dimension"), dimension));
        if (q != null && !q.isBlank()) spec = spec.and(bartWorksSearch(q));

        Page<GtBartWorksOreEntity> result = bartWorksOreRepository.findAll(
                spec,
                pageable(page, 50, Sort.by("entryType").ascending()
                        .and(Sort.by("dimensionDisplayName").ascending())
                        .and(Sort.by("resultDisplayName").ascending())
                        .and(Sort.by("entryId").ascending())));
        List<GtBartWorksOreSummary> items = result.stream().map(o -> bartWorksSummary(dataset, o)).toList();
        return new PageResponse<>(items, pageIndex(page), pageSize(page, 50), result.getTotalElements());
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
                ore.getEntryId(), ore.getEntryType(), ore.getWorldgenName(), ore.getDimension(),
                ore.getDimensionDisplayName(), itemRef(dataset, ore.getResultItemVariantId()),
                ore.getHeightMin(), ore.getHeightMax(), ore.getWeight(), ore.getDensity(), ore.getSize(),
                ore.getAmountPerChunk(), layers);
    }

    private GtOreVeinSummary oreVeinSummary(DatasetSummary dataset, GtOreVeinEntity vein) {
        Optional<GtOreVeinLayerEntity> primary = oreVeinLayerRepository.findById(
                new GtOreVeinLayerEntity.OreVeinLayerId(dataset.datasetId(), vein.getVeinName(), "primary"));
        return new GtOreVeinSummary(
                vein.getVeinName(), vein.getDisplayName(), vein.getHeightMin(), vein.getHeightMax(), vein.getWeight(),
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
                ore.getOreGenName(), ore.getMaterialName(), ore.getAmountPerChunk(), ore.getHeightMin(), ore.getHeightMax(),
                itemRef(dataset, ore.getSmallOreItemVariantId()), smallOreDimensions(dataset, ore.getOreGenName()));
    }

    private GtUndergroundFluidSummary undergroundFluidSummary(DatasetSummary dataset, GtUndergroundFluidBrowserEntity fluid) {
        return new GtUndergroundFluidSummary(
                fluid.getFluidId(),
                fluid.getDimension(),
                new GtFluidRef(
                        fluid.getFluidVariantId(),
                        fluid.getFluidId(),
                        fluid.getFluidDisplayName(),
                        assetUrlBuilder.build(dataset, fluid.getFluidAssetPath(), null)),
                new GtDimensionRef(
                        fluid.getDimension(),
                        fluid.getDimensionFullName(),
                        fluid.getDimensionDisplayName(),
                        fluid.getDimensionDisplayAbbr(),
                        fluid.getDimensionIconItemVariantId(),
                        itemRef(dataset, fluid.getDimensionIconItemVariantId()).assetUrl(),
                        fluid.getDimensionSortOrder() == null ? Integer.MAX_VALUE : fluid.getDimensionSortOrder()),
                fluid.getChance(),
                fluid.getMinAmount(),
                fluid.getMaxAmount());
    }

    private GtBartWorksOreSummary bartWorksSummary(DatasetSummary dataset, GtBartWorksOreEntity ore) {
        return new GtBartWorksOreSummary(
                ore.getEntryId(), ore.getEntryType(), ore.getWorldgenName(), ore.getDimension(), ore.getDimensionDisplayName(),
                itemRef(dataset, ore.getResultItemVariantId()), ore.getHeightMin(), ore.getHeightMax(), ore.getWeight(),
                ore.getDensity(), ore.getSize(), ore.getAmountPerChunk());
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

    private GtDimensionRef dimensionRef(DatasetSummary dataset, String dimension) {
        return dimensionRepository.findById(new GtDimensionDisplayEntity.DimensionDisplayId(dataset.datasetId(), dimension))
                .map(d -> new GtDimensionRef(
                        d.getDimension(), d.getFullName(), d.getDisplayName(), d.getDisplayAbbr(),
                        d.getIconItemVariantId(), itemRef(dataset, d.getIconItemVariantId()).assetUrl(), d.getSortOrder()))
                .orElse(new GtDimensionRef(dimension, dimension, dimension, dimension, "", null, Integer.MAX_VALUE));
    }

    private GtItemRef itemRef(DatasetSummary dataset, String itemVariantId) {
        if (itemVariantId == null || itemVariantId.isBlank()) {
            return new GtItemRef("", "", null);
        }
        return itemVariantRepository.findById(new ItemVariantBrowserEntity.ItemVariantId(dataset.datasetId(), itemVariantId))
                .map(i -> new GtItemRef(
                        i.getItemVariantId(), i.getDisplayName(),
                        assetUrlBuilder.build(dataset, i.getAssetPath(), i.getAssetSha256())))
                .orElse(new GtItemRef(itemVariantId, itemVariantId, null));
    }

    private GtFluidRef fluidRef(DatasetSummary dataset, String fluidVariantId) {
        if (fluidVariantId == null || fluidVariantId.isBlank()) {
            return new GtFluidRef("", "", "", null);
        }
        return fluidVariantRepository.findById(new FluidVariantBrowserEntity.FluidVariantId(dataset.datasetId(), fluidVariantId))
                .map(f -> new GtFluidRef(
                        f.getFluidVariantId(), f.getFluidId(), f.getDisplayName(),
                        assetUrlBuilder.build(dataset, f.getAssetPath(), null)))
                .orElse(new GtFluidRef(fluidVariantId, "", fluidVariantId, null));
    }

    private static <T> Specification<T> hasDatasetId(String datasetId) {
        return (root, query, cb) -> cb.equal(root.get("datasetId"), datasetId);
    }

    private static Specification<GtOreVeinEntity> veinSearch(String q) {
        String pattern = "%" + q.toLowerCase() + "%";
        return (root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("displayName")), pattern),
                cb.like(cb.lower(root.get("veinName")), pattern));
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

    private static Specification<GtOreVeinEntity> veinHasMaterial(String datasetId, String materialName) {
        return (root, cq, cb) -> {
            var sub = cq.subquery(String.class);
            var layer = sub.from(GtOreVeinLayerEntity.class);
            sub.select(layer.get("veinName"));
            sub.where(
                    cb.equal(layer.get("datasetId"), datasetId),
                    cb.equal(layer.get("materialName"), materialName),
                    cb.equal(layer.get("veinName"), root.get("veinName")));
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
        return (root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("fluidDisplayName")), pattern),
                cb.like(cb.lower(root.get("fluidId")), pattern),
                cb.like(cb.lower(root.get("dimensionDisplayName")), pattern));
    }

    private static Specification<GtBartWorksOreEntity> bartWorksSearch(String q) {
        String pattern = "%" + q.toLowerCase() + "%";
        return (root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("resultDisplayName")), pattern),
                cb.like(cb.lower(root.get("worldgenName")), pattern),
                cb.like(cb.lower(root.get("dimensionDisplayName")), pattern));
    }

    private static Pageable pageable(PageRequest page, int defaultSize, Sort sort) {
        return org.springframework.data.domain.PageRequest.of(pageIndex(page), pageSize(page, defaultSize), sort);
    }

    private static int pageIndex(PageRequest page) {
        return page != null ? page.page() : 0;
    }

    private static int pageSize(PageRequest page, int defaultSize) {
        return page != null ? page.size() : defaultSize;
    }
}
