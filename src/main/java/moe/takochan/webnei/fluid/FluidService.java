package moe.takochan.webnei.fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.persistence.criteria.Predicate;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.ModOptionDto;
import moe.takochan.webnei.common.NotFoundException;
import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.dataset.DatasetService;
import moe.takochan.webnei.dataset.DatasetSummary;
import moe.takochan.webnei.gtore.GtDimensionRef;
import moe.takochan.webnei.gtore.GtUndergroundFluidBrowserEntity;
import moe.takochan.webnei.gtore.GtUndergroundFluidBrowserRepository;
import moe.takochan.webnei.item.ItemVariantBrowserEntity;
import moe.takochan.webnei.item.ItemVariantRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class FluidService {

    private final DatasetService datasetService;
    private final FluidVariantRepository fluidRepo;
    private final FluidModOptionRepository modOptionRepo;
    private final GtUndergroundFluidBrowserRepository undergroundFluidRepo;
    private final ItemVariantRepository itemVariantRepo;
    private final AssetUrlBuilder assetUrlBuilder;

    public FluidService(DatasetService datasetService, FluidVariantRepository fluidRepo,
                        FluidModOptionRepository modOptionRepo,
                        GtUndergroundFluidBrowserRepository undergroundFluidRepo,
                        ItemVariantRepository itemVariantRepo,
                        AssetUrlBuilder assetUrlBuilder) {
        this.datasetService = datasetService;
        this.fluidRepo = fluidRepo;
        this.modOptionRepo = modOptionRepo;
        this.undergroundFluidRepo = undergroundFluidRepo;
        this.itemVariantRepo = itemVariantRepo;
        this.assetUrlBuilder = assetUrlBuilder;
    }

    public PageResponse<FluidSummary> list(String datasetId, FluidQuery query, PageRequest page) {
        DatasetSummary dataset = datasetService.requireById(datasetId);

        Specification<FluidVariantBrowserEntity> spec = hasDatasetId(datasetId);
        if (query.modId() != null && !query.modId().isBlank()) {
            spec = spec.and(modIdEq(query.modId()));
        }
        if (query.q() != null && !query.q().isBlank()) {
            spec = spec.and(textSearch(query.q()));
        }

        int pageIndex = page != null ? page.page() : 0;
        int pageSize = page != null ? page.size() : 100;
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageIndex, pageSize,
                Sort.by("modId").ascending()
                        .and(Sort.by("displayName").ascending())
                        .and(Sort.by("fluidVariantId").ascending()));

        Page<FluidVariantBrowserEntity> result = fluidRepo.findAll(spec, pageable);
        Map<String, String> modNames = modOptionRepo.findByDatasetIdOrderByNameAscModIdAsc(datasetId)
                .stream()
                .collect(java.util.stream.Collectors.toMap(FluidModOptionEntity::getModId, FluidModOptionEntity::getName));
        List<FluidSummary> items = result.stream()
                .map(e -> toSummary(e, dataset, modNames))
                .toList();
        return new PageResponse<>(items, pageIndex, pageSize, result.getTotalElements());
    }

    public FluidDetail detail(String datasetId, String fluidVariantId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        FluidVariantBrowserEntity e = fluidRepo.findById(new FluidVariantBrowserEntity.FluidVariantId(datasetId, fluidVariantId))
                .orElseThrow(() -> new NotFoundException("Fluid variant not found: " + fluidVariantId));
        return new FluidDetail(
                e.getFluidVariantId(),
                e.getFluidId(),
                e.getModId(),
                modName(datasetId, e.getModId()),
                e.getRegistryName(),
                e.getUnlocalizedName(),
                e.getDisplayName(),
                e.isGaseous(),
                e.getDensity(),
                e.getTemperature(),
                e.getViscosity(),
                e.getLuminosity(),
                e.getRuntimeFluidId(),
                e.getNbtHash(),
                e.getNbtText(),
                e.getChemicalExpression(),
                assetUrlBuilder.build(dataset, e.getAssetPath(), null),
                undergroundResources(datasetId, e.getFluidId(), e.getRegistryName()));
    }

    public List<ModOptionDto> listMods(String datasetId) {
        datasetService.requireById(datasetId);
        return modOptionRepo.findByDatasetIdOrderByNameAscModIdAsc(datasetId)
                .stream()
                .map(e -> new ModOptionDto(e.getModId(), e.getName()))
                .toList();
    }

    private FluidSummary toSummary(FluidVariantBrowserEntity e, DatasetSummary dataset, Map<String, String> modNames) {
        return new FluidSummary(
                e.getFluidVariantId(), e.getFluidId(), e.getModId(), modNames.getOrDefault(e.getModId(), e.getModId()),
                e.getRegistryName(), e.getDisplayName(), e.isGaseous(),
                e.getDensity(), e.getTemperature(), e.getViscosity(), e.getLuminosity(),
                e.getNbtHash(),
                assetUrlBuilder.build(dataset, e.getAssetPath(), null));
    }

    private String modName(String datasetId, String modId) {
        return modOptionRepo.findByDatasetIdAndModId(datasetId, modId)
                .map(FluidModOptionEntity::getName)
                .orElse(modId);
    }

    private List<FluidUndergroundResource> undergroundResources(
            String datasetId, String fluidId, String registryName) {
        Sort sort = Sort.by("dimensionSortOrder").ascending()
                .and(Sort.by("dimensionDisplayName").ascending())
                .and(Sort.by("dimension").ascending());
        List<String> candidates = java.util.stream.Stream.of(fluidId, registryName, stripNamespace(fluidId), stripNamespace(registryName))
                .filter(v -> v != null && !v.isBlank())
                .distinct()
                .toList();
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return candidates.stream()
                .flatMap(id -> undergroundFluidRepo.findByDatasetIdAndFluidId(datasetId, id, sort).stream())
                .collect(java.util.stream.Collectors.toMap(
                        f -> f.getFluidId() + "|" + f.getDimension(),
                        f -> f,
                        (a, b) -> a,
                        java.util.LinkedHashMap::new))
                .values()
                .stream()
                .map(f -> new FluidUndergroundResource(
                        f.getFluidId(),
                        f.getDimension(),
                        dimensionRef(dataset, f),
                        f.getChance(),
                        f.getMinAmount(),
                        f.getMaxAmount()))
                .toList();
    }

    private GtDimensionRef dimensionRef(DatasetSummary dataset, GtUndergroundFluidBrowserEntity fluid) {
        return new GtDimensionRef(
                fluid.getDimension(),
                fluid.getDimensionFullName() != null ? fluid.getDimensionFullName() : fluid.getDimension(),
                fluid.getDimensionDisplayName() != null ? fluid.getDimensionDisplayName() : fluid.getDimension(),
                fluid.getDimensionDisplayAbbr() != null ? fluid.getDimensionDisplayAbbr() : fluid.getDimension(),
                fluid.getDimensionIconItemVariantId(),
                itemAssetUrl(dataset, fluid.getDimensionIconItemVariantId()),
                fluid.getDimensionSortOrder() == null ? Integer.MAX_VALUE : fluid.getDimensionSortOrder());
    }

    private String itemAssetUrl(DatasetSummary dataset, String itemVariantId) {
        if (itemVariantId == null || itemVariantId.isBlank()) return null;
        return itemVariantRepo.findById(new ItemVariantBrowserEntity.ItemVariantId(dataset.datasetId(), itemVariantId))
                .map(i -> assetUrlBuilder.build(dataset, i.getAssetPath(), i.getAssetSha256()))
                .orElse(null);
    }

    private static String stripNamespace(String value) {
        if (value == null) return null;
        int idx = value.indexOf(':');
        return idx >= 0 ? value.substring(idx + 1) : value;
    }

    private static Specification<FluidVariantBrowserEntity> hasDatasetId(String datasetId) {
        return (root, query, cb) -> cb.equal(root.get("datasetId"), datasetId);
    }

    private static Specification<FluidVariantBrowserEntity> modIdEq(String modId) {
        return (root, query, cb) -> cb.equal(root.get("modId"), modId);
    }

    private static Specification<FluidVariantBrowserEntity> textSearch(String q) {
        String pattern = "%" + q.trim().toLowerCase() + "%";
        return (root, cq, cb) -> {
            Predicate[] conditions = {
                    cb.like(root.get("fluidSearchText"), pattern),
                    cb.like(root.get("variantSearchText"), pattern)
            };
            return cb.or(conditions);
        };
    }
}
