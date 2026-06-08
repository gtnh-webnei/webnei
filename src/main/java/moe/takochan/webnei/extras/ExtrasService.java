package moe.takochan.webnei.extras;

import java.util.List;
import java.util.Map;

import jakarta.persistence.criteria.Predicate;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.EntityRefService;
import moe.takochan.webnei.common.FluidRef;
import moe.takochan.webnei.common.ItemRef;
import moe.takochan.webnei.dataset.DatasetSummary;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ExtrasService {

    private final ItemOreDictionaryNameRepository oreDictRepo;
    private final FluidContainerBrowserRepository containerRepo;
    private final ItemAspectBrowserRepository aspectRepo;
    private final FluidBlockBrowserRepository blockRepo;
    private final AssetUrlBuilder assetUrlBuilder;
    private final EntityRefService entityRefService;

    public ExtrasService(ItemOreDictionaryNameRepository oreDictRepo,
                         FluidContainerBrowserRepository containerRepo,
                         ItemAspectBrowserRepository aspectRepo,
                         FluidBlockBrowserRepository blockRepo,
                         AssetUrlBuilder assetUrlBuilder,
                         EntityRefService entityRefService) {
        this.oreDictRepo = oreDictRepo;
        this.containerRepo = containerRepo;
        this.aspectRepo = aspectRepo;
        this.blockRepo = blockRepo;
        this.assetUrlBuilder = assetUrlBuilder;
        this.entityRefService = entityRefService;
    }

    public ItemExtras itemExtras(DatasetSummary dataset, String itemVariantId) {
        String datasetId = dataset.datasetId();

        List<String> oreNames = oreDictRepo.findByDatasetIdAndItemVariantIdOrderByOreNameAsc(datasetId, itemVariantId)
                .stream()
                .map(ItemOreDictionaryNameEntity::getOreName)
                .toList();
        List<ItemRelatedFluidEntry> relatedFluids = relatedFluidsForItem(dataset, itemVariantId);
        List<AspectEntry> aspects = aspectRepo.findByDatasetIdAndItemVariantId(
                        datasetId, itemVariantId,
                        Sort.by("amount").descending().and(Sort.by("aspectId").ascending()))
                .stream()
                .map(e -> toAspect(e, dataset))
                .toList();

        return new ItemExtras(
                oreNames,
                relatedFluids,
                aspects);
    }

    public FluidExtras fluidExtras(DatasetSummary dataset, String fluidVariantId) {
        String datasetId = dataset.datasetId();
        List<FluidContainerBrowserEntity> containerRows = containerRepo.findByDatasetIdAndFluidVariantId(
                datasetId, fluidVariantId,
                Sort.by("amount").ascending().and(Sort.by("containerItemVariantId").ascending()));
        Map<String, ItemRef> containerRefs = entityRefService.itemRefs(dataset, containerRows.stream()
                .map(FluidContainerBrowserEntity::getContainerItemVariantId)
                .toList());
        List<FluidContainerEntry> containers = toContainers(containerRows, containerRefs);

        List<FluidBlockBrowserEntity> blockRows = blockRepo.findByDatasetIdAndFluidVariantId(
                datasetId, fluidVariantId, Sort.by("blockItemVariantId").ascending());
        Map<String, ItemRef> blockRefs = entityRefService.itemRefs(dataset, blockRows.stream()
                .map(FluidBlockBrowserEntity::getBlockItemVariantId)
                .toList());
        List<FluidBlockEntry> blocks = blockRows.stream()
                .map(e -> toBlock(e, blockRefs))
                .toList();

        return new FluidExtras(
                containers,
                blocks);
    }

    private List<ItemRelatedFluidEntry> relatedFluidsForItem(DatasetSummary dataset, String itemVariantId) {
        Specification<FluidContainerBrowserEntity> spec = containerForItem(dataset.datasetId(), itemVariantId);
        Sort sort = Sort.by("fluidVariantId").ascending();
        List<FluidContainerBrowserEntity> rows = containerRepo.findAll(spec, sort);
        List<String> fluidIds = rows.stream()
                .map(FluidContainerBrowserEntity::getFluidVariantId)
                .distinct()
                .toList();
        Map<String, FluidRef> fluids = entityRefService.fluidRefs(dataset, fluidIds);
        return fluidIds.stream()
                .map(fluidVariantId -> new ItemRelatedFluidEntry(fluids.getOrDefault(fluidVariantId, entityRefService.fluidRef(dataset, fluidVariantId))))
                .toList();
    }

    private List<FluidContainerEntry> toContainers(List<FluidContainerBrowserEntity> rows, Map<String, ItemRef> containerRefs) {
        return rows.stream()
                .map(e -> toContainer(e, containerRefs))
                .toList();
    }

    private FluidContainerEntry toContainer(FluidContainerBrowserEntity e, Map<String, ItemRef> containerRefs) {
        return new FluidContainerEntry(
                containerRefs.getOrDefault(e.getContainerItemVariantId(), fallbackItemRef(e.getContainerItemVariantId(), e.getContainerDisplayName())),
                e.getAmount());
    }

    private AspectEntry toAspect(ItemAspectBrowserEntity e, DatasetSummary dataset) {
        return new AspectEntry(
                e.getAspectId(), e.getName(), e.getDescription(), e.isPrimal(), e.getAmount(),
                e.getIconItemVariantId(),
                assetUrlBuilder.build(dataset, e.getIconAssetPath(), e.getIconAssetSha256()));
    }

    private FluidBlockEntry toBlock(FluidBlockBrowserEntity e, Map<String, ItemRef> blockRefs) {
        return new FluidBlockEntry(
                blockRefs.getOrDefault(e.getBlockItemVariantId(), fallbackItemRef(e.getBlockItemVariantId(), e.getBlockDisplayName())));
    }

    private static ItemRef fallbackItemRef(String itemVariantId, String displayName) {
        return new ItemRef(itemVariantId, null, null, displayName, null, null);
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
