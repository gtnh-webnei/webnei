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
import moe.takochan.webnei.model.dto.FluidDetail;
import moe.takochan.webnei.model.dto.FluidExtras;
import moe.takochan.webnei.model.dto.FluidSummary;
import moe.takochan.webnei.model.dto.FluidUndergroundResource;
import moe.takochan.webnei.model.dto.GtDimensionRef;
import moe.takochan.webnei.model.dto.ItemRef;
import moe.takochan.webnei.model.dto.ModOptionDto;
import moe.takochan.webnei.model.dto.PageRequest;
import moe.takochan.webnei.model.entity.table.FluidSearchDocumentEntity;
import moe.takochan.webnei.model.entity.view.FluidDetailEntity;
import moe.takochan.webnei.model.entity.view.FluidListEntity;
import moe.takochan.webnei.model.entity.view.GtUndergroundFluidBrowserEntity;
import moe.takochan.webnei.model.query.FluidQuery;
import moe.takochan.webnei.repository.table.FluidSearchDocumentRepository;
import moe.takochan.webnei.repository.view.FluidDetailRepository;
import moe.takochan.webnei.repository.view.FluidListRepository;
import moe.takochan.webnei.repository.view.FluidModOptionRepository;
import moe.takochan.webnei.repository.view.GtUndergroundFluidBrowserRepository;
import moe.takochan.webnei.service.EntityRefService;
import moe.takochan.webnei.service.ExtrasService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class FluidService {

    private final FluidListRepository fluidListRepo;
    private final FluidSearchDocumentRepository searchRepo;
    private final FluidDetailRepository fluidDetailRepo;
    private final FluidModOptionRepository modOptionRepo;
    private final GtUndergroundFluidBrowserRepository undergroundFluidRepo;
    private final AssetUrlBuilder assetUrlBuilder;
    private final ExtrasService extrasService;
    private final EntityRefService entityRefService;

    public FluidService(FluidListRepository fluidListRepo,
                        FluidSearchDocumentRepository searchRepo,
                        FluidDetailRepository fluidDetailRepo,
                        FluidModOptionRepository modOptionRepo,
                        GtUndergroundFluidBrowserRepository undergroundFluidRepo,
                        AssetUrlBuilder assetUrlBuilder,
                        ExtrasService extrasService,
                        EntityRefService entityRefService) {
        this.fluidListRepo = fluidListRepo;
        this.searchRepo = searchRepo;
        this.fluidDetailRepo = fluidDetailRepo;
        this.modOptionRepo = modOptionRepo;
        this.undergroundFluidRepo = undergroundFluidRepo;
        this.assetUrlBuilder = assetUrlBuilder;
        this.extrasService = extrasService;
        this.entityRefService = entityRefService;
    }

    public PageResponse<FluidSummary> list(DatasetSummary dataset, FluidQuery query, PageRequest page) {
        if (query.q() != null && !query.q().isBlank()) {
            return search(dataset, query, page);
        }

        String datasetId = dataset.datasetId();
        Specification<FluidListEntity> spec = hasDatasetId(datasetId);
        if (query.modId() != null && !query.modId().isBlank()) {
            spec = spec.and(modIdEq(query.modId()));
        }

        int pageIndex = page != null ? page.page() : 0;
        int pageSize = page != null ? page.size() : 100;
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageIndex, pageSize,
                Sort.by("modId").ascending()
                        .and(Sort.by("displayName").ascending())
                        .and(Sort.by("fluidVariantId").ascending()));

        Page<FluidListEntity> result = fluidListRepo.findAll(spec, pageable);
        List<FluidSummary> items = result.stream()
                .map(e -> toSummary(e, dataset))
                .toList();
        return new PageResponse<>(items, pageIndex, pageSize, result.getTotalElements());
    }

    private PageResponse<FluidSummary> search(DatasetSummary dataset, FluidQuery query, PageRequest page) {
        String datasetId = dataset.datasetId();
        int pageIndex = page != null ? page.page() : 0;
        int pageSize = page != null ? page.size() : 100;
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageIndex, pageSize,
                Sort.by("modId").ascending()
                        .and(Sort.by("displayName").ascending())
                        .and(Sort.by("fluidVariantId").ascending()));
        Page<FluidSearchDocumentEntity> result = searchRepo.findAll(searchSpec(datasetId, query), pageable);
        List<String> ids = result.stream()
                .map(FluidSearchDocumentEntity::getFluidVariantId)
                .toList();
        Map<String, FluidListEntity> rows = fluidListRepo.findAllById(ids.stream()
                        .map(id -> new FluidListEntity.FluidListId(datasetId, id))
                        .toList())
                .stream()
                .collect(Collectors.toMap(FluidListEntity::getFluidVariantId, Function.identity()));
        List<FluidSummary> items = ids.stream()
                .map(rows::get)
                .filter(e -> e != null)
                .map(e -> toSummary(e, dataset))
                .toList();
        return new PageResponse<>(items, pageIndex, pageSize, result.getTotalElements());
    }

    public FluidDetail detail(DatasetSummary dataset, String fluidVariantId) {
        String datasetId = dataset.datasetId();
        FluidDetailEntity e = fluidDetailRepo.findById(new FluidDetailEntity.FluidDetailId(datasetId, fluidVariantId))
                .orElseThrow(() -> new NotFoundException("Fluid variant not found: " + fluidVariantId));
        FluidExtras extras = extrasService.fluidExtras(dataset, fluidVariantId);
        return new FluidDetail(
                e.getFluidId(),
                e.getModId(),
                e.getModName(),
                e.getRegistryName(),
                e.getUnlocalizedName(),
                e.getDisplayName(),
                e.isGaseous(),
                e.getDensity(),
                e.getTemperature(),
                e.getViscosity(),
                e.getLuminosity(),
                e.getRuntimeFluidId(),
                e.getNbtText(),
                e.getChemicalExpression(),
                undergroundResources(dataset, e.getFluidId(), e.getRegistryName()),
                extras.containers(),
                extras.blocks());
    }

    public List<ModOptionDto> listMods(DatasetSummary dataset) {
        return modOptionRepo.findByDatasetIdOrderByNameAscModIdAsc(dataset.datasetId())
                .stream()
                .map(e -> new ModOptionDto(e.getModId(), e.getName()))
                .toList();
    }

    private FluidSummary toSummary(FluidListEntity e, DatasetSummary dataset) {
        return new FluidSummary(
                e.getFluidVariantId(), e.getFluidId(), e.getModId(), e.getModName(),
                e.getRegistryName(), e.getDisplayName(), e.isGaseous(),
                e.getDensity(), e.getTemperature(), e.getViscosity(), e.getLuminosity(),
                assetUrlBuilder.build(dataset, e.getAssetPath(), null));
    }

    private List<FluidUndergroundResource> undergroundResources(
            DatasetSummary dataset, String fluidId, String registryName) {
        String datasetId = dataset.datasetId();
        Sort sort = Sort.by("dimensionSortOrder").ascending()
                .and(Sort.by("dimensionDisplayName").ascending())
                .and(Sort.by("dimension").ascending());
        List<String> candidates = java.util.stream.Stream.of(fluidId, registryName, stripNamespace(fluidId), stripNamespace(registryName))
                .filter(v -> v != null && !v.isBlank())
                .distinct()
                .toList();
        List<GtUndergroundFluidBrowserEntity> rows = candidates.stream()
                .flatMap(id -> undergroundFluidRepo.findByDatasetIdAndFluidId(datasetId, id, sort).stream())
                .collect(java.util.stream.Collectors.toMap(
                        f -> f.getFluidId() + "|" + f.getDimension(),
                        f -> f,
                        (a, b) -> a,
                        java.util.LinkedHashMap::new))
                .values()
                .stream()
                .toList();
        Map<String, ItemRef> dimensionIcons = entityRefService.itemRefs(
                dataset,
                rows.stream()
                        .map(GtUndergroundFluidBrowserEntity::getDimensionIconItemVariantId)
                        .filter(id -> id != null && !id.isBlank())
                        .distinct()
                        .toList());
        return rows.stream()
                .map(f -> new FluidUndergroundResource(
                        f.getFluidId(),
                        f.getDimension(),
                        dimensionRef(dataset, f, dimensionIcons),
                        f.getChance(),
                        f.getMinAmount(),
                        f.getMaxAmount()))
                .toList();
    }

    private GtDimensionRef dimensionRef(
            DatasetSummary dataset,
            GtUndergroundFluidBrowserEntity fluid,
            Map<String, ItemRef> dimensionIcons) {
        return new GtDimensionRef(
                fluid.getDimension(),
                fluid.getDimensionFullName() != null ? fluid.getDimensionFullName() : fluid.getDimension(),
                fluid.getDimensionDisplayName() != null ? fluid.getDimensionDisplayName() : fluid.getDimension(),
                fluid.getDimensionDisplayAbbr() != null ? fluid.getDimensionDisplayAbbr() : fluid.getDimension(),
                fluid.getDimensionIconItemVariantId(),
                itemAssetUrl(fluid.getDimensionIconItemVariantId(), dimensionIcons),
                fluid.getDimensionSortOrder() == null ? Integer.MAX_VALUE : fluid.getDimensionSortOrder());
    }

    private String itemAssetUrl(
            String itemVariantId,
            Map<String, ItemRef> itemVariants) {
        if (itemVariantId == null || itemVariantId.isBlank()) return null;
        ItemRef item = itemVariants.get(itemVariantId);
        return item == null ? null : item.assetUrl();
    }

    private static String stripNamespace(String value) {
        if (value == null) return null;
        int idx = value.indexOf(':');
        return idx >= 0 ? value.substring(idx + 1) : value;
    }

    private static Specification<FluidListEntity> hasDatasetId(String datasetId) {
        return (root, query, cb) -> cb.equal(root.get("datasetId"), datasetId);
    }

    private static Specification<FluidListEntity> modIdEq(String modId) {
        return (root, query, cb) -> cb.equal(root.get("modId"), modId);
    }

    private static Specification<FluidSearchDocumentEntity> searchSpec(String datasetId, FluidQuery query) {
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
}
