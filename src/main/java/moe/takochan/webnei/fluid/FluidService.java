package moe.takochan.webnei.fluid;

import java.util.ArrayList;
import java.util.List;

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
public class FluidService {

    private final DatasetService datasetService;
    private final FluidVariantRepository fluidRepo;
    private final FluidModOptionRepository modOptionRepo;
    private final AssetUrlBuilder assetUrlBuilder;

    public FluidService(DatasetService datasetService, FluidVariantRepository fluidRepo,
                        FluidModOptionRepository modOptionRepo, AssetUrlBuilder assetUrlBuilder) {
        this.datasetService = datasetService;
        this.fluidRepo = fluidRepo;
        this.modOptionRepo = modOptionRepo;
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
        List<FluidSummary> items = result.stream()
                .map(e -> toSummary(e, dataset))
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
                assetUrlBuilder.build(dataset, e.getAssetPath(), null));
    }

    public List<ModOptionDto> listMods(String datasetId) {
        datasetService.requireById(datasetId);
        return modOptionRepo.findByDatasetIdOrderByNameAscModIdAsc(datasetId)
                .stream()
                .map(e -> new ModOptionDto(e.getModId(), e.getName()))
                .toList();
    }

    private FluidSummary toSummary(FluidVariantBrowserEntity e, DatasetSummary dataset) {
        return new FluidSummary(
                e.getFluidVariantId(), e.getFluidId(), e.getModId(),
                e.getRegistryName(), e.getDisplayName(), e.isGaseous(),
                e.getDensity(), e.getTemperature(), e.getViscosity(), e.getLuminosity(),
                e.getNbtHash(),
                assetUrlBuilder.build(dataset, e.getAssetPath(), null));
    }

    private static Specification<FluidVariantBrowserEntity> hasDatasetId(String datasetId) {
        return (root, query, cb) -> cb.equal(root.get("datasetId"), datasetId);
    }

    private static Specification<FluidVariantBrowserEntity> modIdEq(String modId) {
        return (root, query, cb) -> cb.equal(root.get("modId"), modId);
    }

    private static Specification<FluidVariantBrowserEntity> textSearch(String q) {
        String pattern = "%" + q.toLowerCase() + "%";
        return (root, cq, cb) -> {
            Predicate[] conditions = {
                    cb.like(cb.lower(root.get("displayName")), pattern),
                    cb.like(cb.lower(root.get("registryName")), pattern),
                    cb.like(cb.lower(root.get("fluidId")), pattern),
                    cb.like(cb.lower(root.get("chemicalExpression")), pattern)
            };
            return cb.or(conditions);
        };
    }
}
