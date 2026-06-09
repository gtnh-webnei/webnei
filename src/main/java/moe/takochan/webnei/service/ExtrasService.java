package moe.takochan.webnei.service;

import jakarta.persistence.criteria.Predicate;
import java.util.List;
import java.util.Map;
import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.model.dto.AspectEntry;
import moe.takochan.webnei.model.dto.DatasetSummary;
import moe.takochan.webnei.model.dto.FluidBlockEntry;
import moe.takochan.webnei.model.dto.FluidContainerEntry;
import moe.takochan.webnei.model.dto.FluidExtras;
import moe.takochan.webnei.model.dto.FluidRef;
import moe.takochan.webnei.model.dto.ItemExtras;
import moe.takochan.webnei.model.dto.ItemRef;
import moe.takochan.webnei.model.dto.ItemRelatedFluidEntry;
import moe.takochan.webnei.model.entity.view.FluidBlockBrowserEntity;
import moe.takochan.webnei.model.entity.view.FluidContainerBrowserEntity;
import moe.takochan.webnei.model.entity.view.ItemAspectBrowserEntity;
import moe.takochan.webnei.model.entity.view.ItemOreDictionaryNameEntity;
import moe.takochan.webnei.repository.view.FluidBlockBrowserRepository;
import moe.takochan.webnei.repository.view.FluidContainerBrowserRepository;
import moe.takochan.webnei.repository.view.ItemAspectBrowserRepository;
import moe.takochan.webnei.repository.view.ItemOreDictionaryNameRepository;
import moe.takochan.webnei.service.EntityRefService;
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
                .filter(java.util.Objects::nonNull)
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
                .map(fluids::get)
                .filter(java.util.Objects::nonNull)
                .map(ItemRelatedFluidEntry::new)
                .toList();
    }

    private List<FluidContainerEntry> toContainers(List<FluidContainerBrowserEntity> rows, Map<String, ItemRef> containerRefs) {
        return rows.stream()
                .map(e -> toContainer(e, containerRefs))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private FluidContainerEntry toContainer(FluidContainerBrowserEntity e, Map<String, ItemRef> containerRefs) {
        ItemRef container = containerRefs.get(e.getContainerItemVariantId());
        return container == null ? null : new FluidContainerEntry(container, e.getAmount());
    }

    private AspectEntry toAspect(ItemAspectBrowserEntity e, DatasetSummary dataset) {
        return new AspectEntry(
                e.getAspectId(), e.getName(), e.getDescription(), e.isPrimal(), e.getAmount(),
                e.getIconItemVariantId(),
                assetUrlBuilder.build(dataset, e.getIconAssetPath(), e.getIconAssetSha256()));
    }

    private FluidBlockEntry toBlock(FluidBlockBrowserEntity e, Map<String, ItemRef> blockRefs) {
        ItemRef block = blockRefs.get(e.getBlockItemVariantId());
        return block == null ? null : new FluidBlockEntry(block);
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
