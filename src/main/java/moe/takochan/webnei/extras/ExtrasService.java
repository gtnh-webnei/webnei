package moe.takochan.webnei.extras;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.dataset.DatasetService;
import moe.takochan.webnei.dataset.DatasetSummary;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ExtrasService {

    private static final int CONTAINER_PREVIEW_LIMIT = 50;

    private final DatasetService datasetService;
    private final ItemOreDictionaryNameRepository oreDictRepo;
    private final FluidContainerBrowserRepository containerRepo;
    private final ItemAspectBrowserRepository aspectRepo;
    private final FluidBlockBrowserRepository blockRepo;
    private final RecipeLookupCountRepository recipeLookupCountRepo;
    private final AssetUrlBuilder assetUrlBuilder;

    public ExtrasService(DatasetService datasetService,
                         ItemOreDictionaryNameRepository oreDictRepo,
                         FluidContainerBrowserRepository containerRepo,
                         ItemAspectBrowserRepository aspectRepo,
                         FluidBlockBrowserRepository blockRepo,
                         RecipeLookupCountRepository recipeLookupCountRepo,
                         AssetUrlBuilder assetUrlBuilder) {
        this.datasetService = datasetService;
        this.oreDictRepo = oreDictRepo;
        this.containerRepo = containerRepo;
        this.aspectRepo = aspectRepo;
        this.blockRepo = blockRepo;
        this.recipeLookupCountRepo = recipeLookupCountRepo;
        this.assetUrlBuilder = assetUrlBuilder;
    }

    public ItemExtras itemExtras(String datasetId, String itemVariantId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);

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
        Map<String, Long> counts = recipeCounts(datasetId, "item", itemVariantId);

        return new ItemExtras(
                oreNames,
                containers,
                containerTotal,
                aspects,
                counts.getOrDefault("usage", 0L),
                counts.getOrDefault("recipe", 0L));
    }

    public List<FluidContainerEntry> allContainersForItem(String datasetId, String itemVariantId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return listContainersForItem(dataset, itemVariantId, null);
    }

    public FluidExtras fluidExtras(String datasetId, String fluidVariantId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        List<FluidContainerEntry> containers = containerRepo.findByDatasetIdAndFluidVariantId(
                        datasetId, fluidVariantId,
                        Sort.by("amount").ascending().and(Sort.by("containerItemVariantId").ascending()))
                .stream()
                .map(e -> toContainer(e, dataset))
                .toList();
        List<FluidBlockEntry> blocks = blockRepo.findByDatasetIdAndFluidVariantId(
                        datasetId, fluidVariantId, Sort.by("blockItemVariantId").ascending())
                .stream()
                .map(e -> toBlock(e, dataset))
                .toList();
        Map<String, Long> counts = recipeCounts(datasetId, "fluid", fluidVariantId);

        return new FluidExtras(
                containers,
                blocks,
                counts.getOrDefault("usage", 0L),
                counts.getOrDefault("recipe", 0L));
    }

    private List<FluidContainerEntry> listContainersForItem(
            DatasetSummary dataset, String itemVariantId, Integer limit) {
        Specification<FluidContainerBrowserEntity> spec = containerForItem(dataset.datasetId(), itemVariantId);
        Sort sort = Sort.by("amount").ascending()
                .and(Sort.by("fluidVariantId").ascending())
                .and(Sort.by("containerItemVariantId").ascending());
        if (limit == null) {
            return containerRepo.findAll(spec, sort).stream()
                    .map(e -> toContainer(e, dataset))
                    .toList();
        }
        return containerRepo.findAll(spec, org.springframework.data.domain.PageRequest.of(0, limit, sort))
                .stream()
                .map(e -> toContainer(e, dataset))
                .toList();
    }

    private long countContainersForItem(String datasetId, String itemVariantId) {
        return containerRepo.count(containerForItem(datasetId, itemVariantId));
    }

    private Map<String, Long> recipeCounts(String datasetId, String targetDomain, String targetId) {
        return recipeLookupCountRepo.findByDatasetIdAndTargetDomainAndTargetId(datasetId, targetDomain, targetId)
                .stream()
                .collect(Collectors.toMap(RecipeLookupCountEntity::getLookupKind, RecipeLookupCountEntity::getRecipeCount));
    }

    private FluidContainerEntry toContainer(FluidContainerBrowserEntity e, DatasetSummary dataset) {
        return new FluidContainerEntry(
                e.getFluidVariantId(),
                e.getFluidDisplayName(),
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
                assetUrlBuilder.build(dataset, e.getBlockAssetPath(), e.getBlockAssetSha256()));
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
