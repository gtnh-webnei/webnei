package moe.takochan.webnei.extras;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.dataset.DatasetSummary;
import moe.takochan.webnei.fluid.FluidModOptionEntity;
import moe.takochan.webnei.fluid.FluidModOptionRepository;
import moe.takochan.webnei.fluid.FluidVariantBrowserEntity;
import moe.takochan.webnei.fluid.FluidVariantRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ExtrasService {

    private static final int CONTAINER_PREVIEW_LIMIT = 50;

    private final ItemOreDictionaryNameRepository oreDictRepo;
    private final FluidContainerBrowserRepository containerRepo;
    private final FluidVariantRepository fluidVariantRepo;
    private final FluidModOptionRepository fluidModOptionRepo;
    private final ItemAspectBrowserRepository aspectRepo;
    private final FluidBlockBrowserRepository blockRepo;
    private final AssetUrlBuilder assetUrlBuilder;

    public ExtrasService(ItemOreDictionaryNameRepository oreDictRepo,
                         FluidContainerBrowserRepository containerRepo,
                         FluidVariantRepository fluidVariantRepo,
                         FluidModOptionRepository fluidModOptionRepo,
                         ItemAspectBrowserRepository aspectRepo,
                         FluidBlockBrowserRepository blockRepo,
                         AssetUrlBuilder assetUrlBuilder) {
        this.oreDictRepo = oreDictRepo;
        this.containerRepo = containerRepo;
        this.fluidVariantRepo = fluidVariantRepo;
        this.fluidModOptionRepo = fluidModOptionRepo;
        this.aspectRepo = aspectRepo;
        this.blockRepo = blockRepo;
        this.assetUrlBuilder = assetUrlBuilder;
    }

    public ItemExtras itemExtras(DatasetSummary dataset, String itemVariantId) {
        String datasetId = dataset.datasetId();

        List<String> oreNames = oreDictRepo.findByDatasetIdAndItemVariantIdOrderByOreNameAsc(datasetId, itemVariantId)
                .stream()
                .map(ItemOreDictionaryNameEntity::getOreName)
                .toList();
        List<FluidContainerEntry> containers = listContainersForItem(dataset, itemVariantId, CONTAINER_PREVIEW_LIMIT);
        long containerTotal = containers.size() < CONTAINER_PREVIEW_LIMIT
                ? containers.size()
                : countContainersForItem(datasetId, itemVariantId);
        List<AspectEntry> aspects = aspectRepo.findByDatasetIdAndItemVariantId(
                        datasetId, itemVariantId,
                        Sort.by("amount").descending().and(Sort.by("aspectId").ascending()))
                .stream()
                .map(e -> toAspect(e, dataset))
                .toList();

        return new ItemExtras(
                oreNames,
                containers,
                containerTotal,
                aspects);
    }

    public List<FluidContainerEntry> allContainersForItem(DatasetSummary dataset, String itemVariantId) {
        return listContainersForItem(dataset, itemVariantId, null);
    }

    public FluidExtras fluidExtras(DatasetSummary dataset, String fluidVariantId) {
        String datasetId = dataset.datasetId();
        List<FluidContainerBrowserEntity> containerRows = containerRepo.findByDatasetIdAndFluidVariantId(
                datasetId, fluidVariantId,
                Sort.by("amount").ascending().and(Sort.by("containerItemVariantId").ascending()));
        List<FluidContainerEntry> containers = toContainers(containerRows, dataset);
        List<FluidBlockEntry> blocks = blockRepo.findByDatasetIdAndFluidVariantId(
                        datasetId, fluidVariantId, Sort.by("blockItemVariantId").ascending())
                .stream()
                .map(e -> toBlock(e, dataset))
                .toList();

        return new FluidExtras(
                containers,
                blocks);
    }

    private List<FluidContainerEntry> listContainersForItem(
            DatasetSummary dataset, String itemVariantId, Integer limit) {
        Specification<FluidContainerBrowserEntity> spec = containerForItem(dataset.datasetId(), itemVariantId);
        Sort sort = Sort.by("amount").ascending()
                .and(Sort.by("fluidVariantId").ascending())
                .and(Sort.by("containerItemVariantId").ascending());
        if (limit == null) {
            return toContainers(containerRepo.findAll(spec, sort), dataset);
        }
        return toContainers(
                containerRepo.findAll(spec, org.springframework.data.domain.PageRequest.of(0, limit, sort)).stream()
                        .toList(),
                dataset);
    }

    private long countContainersForItem(String datasetId, String itemVariantId) {
        return containerRepo.count(containerForItem(datasetId, itemVariantId));
    }

    private List<FluidContainerEntry> toContainers(List<FluidContainerBrowserEntity> rows, DatasetSummary dataset) {
        List<FluidVariantBrowserEntity.FluidVariantId> fluidIds = rows.stream()
                .map(e -> new FluidVariantBrowserEntity.FluidVariantId(dataset.datasetId(), e.getFluidVariantId()))
                .distinct()
                .toList();
        Map<String, FluidVariantBrowserEntity> fluids = fluidVariantRepo.findAllById(fluidIds).stream()
                .collect(Collectors.toMap(FluidVariantBrowserEntity::getFluidVariantId, Function.identity()));
        Map<String, String> modNames = fluidModOptionRepo.findByDatasetIdOrderByNameAscModIdAsc(dataset.datasetId())
                .stream()
                .collect(Collectors.toMap(FluidModOptionEntity::getModId, FluidModOptionEntity::getName));
        return rows.stream()
                .map(e -> toContainer(e, dataset, fluids.get(e.getFluidVariantId()), modNames))
                .toList();
    }

    private FluidContainerEntry toContainer(
            FluidContainerBrowserEntity e,
            DatasetSummary dataset,
            FluidVariantBrowserEntity fluid,
            Map<String, String> modNames) {
        return new FluidContainerEntry(
                e.getFluidVariantId(),
                fluid != null ? fluid.getFluidId() : "",
                fluid != null ? fluid.getModId() : null,
                fluid != null ? modNames.getOrDefault(fluid.getModId(), fluid.getModId()) : null,
                e.getFluidDisplayName(),
                fluid != null ? fluid.isGaseous() : null,
                fluid != null ? fluid.getTemperature() : null,
                fluid != null ? assetUrlBuilder.build(dataset, fluid.getAssetPath(), null) : null,
                e.getContainerItemVariantId(),
                e.getContainerDisplayName(),
                assetUrlBuilder.build(dataset, e.getContainerAssetPath(), e.getContainerAssetSha256()),
                e.getEmptyContainerItemVariantId(),
                e.getEmptyContainerDisplayName(),
                assetUrlBuilder.build(dataset, e.getEmptyContainerAssetPath(), e.getEmptyContainerAssetSha256()),
                e.getAmount());
    }

    private AspectEntry toAspect(ItemAspectBrowserEntity e, DatasetSummary dataset) {
        return new AspectEntry(
                e.getAspectId(), e.getName(), e.getDescription(), e.isPrimal(), e.getAmount(),
                e.getIconItemVariantId(),
                assetUrlBuilder.build(dataset, e.getIconAssetPath(), e.getIconAssetSha256()));
    }

    private FluidBlockEntry toBlock(FluidBlockBrowserEntity e, DatasetSummary dataset) {
        return new FluidBlockEntry(
                e.getBlockItemVariantId(), e.getBlockDisplayName(),
                assetUrlBuilder.build(dataset, e.getBlockAssetPath(), e.getBlockAssetSha256()),
                e.getBlockTooltipText());
    }

    private static Specification<FluidContainerBrowserEntity> containerForItem(String datasetId, String itemVariantId) {
        return (root, query, cb) -> {
            Predicate dataset = cb.equal(root.get("datasetId"), datasetId);
            Predicate filled = cb.equal(root.get("containerItemVariantId"), itemVariantId);
            Predicate empty = cb.equal(root.get("emptyContainerItemVariantId"), itemVariantId);
            return cb.and(dataset, cb.or(filled, empty));
        };
    }
}
