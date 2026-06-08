package moe.takochan.webnei.extras;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.FluidRef;
import moe.takochan.webnei.common.ItemRef;
import moe.takochan.webnei.dataset.DatasetSummary;
import moe.takochan.webnei.fluid.FluidModOptionEntity;
import moe.takochan.webnei.fluid.FluidModOptionRepository;
import moe.takochan.webnei.fluid.FluidVariantBrowserEntity;
import moe.takochan.webnei.fluid.FluidVariantRepository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ExtrasService {

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

    private List<ItemRelatedFluidEntry> relatedFluidsForItem(DatasetSummary dataset, String itemVariantId) {
        Specification<FluidContainerBrowserEntity> spec = containerForItem(dataset.datasetId(), itemVariantId);
        Sort sort = Sort.by("fluidVariantId").ascending();
        List<FluidContainerBrowserEntity> rows = containerRepo.findAll(spec, sort);
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
                .map(FluidContainerBrowserEntity::getFluidVariantId)
                .distinct()
                .map(fluidVariantId -> toRelatedFluid(dataset, fluidVariantId, fluids.get(fluidVariantId), modNames))
                .toList();
    }

    private ItemRelatedFluidEntry toRelatedFluid(
            DatasetSummary dataset,
            String fluidVariantId,
            FluidVariantBrowserEntity fluid,
            Map<String, String> modNames) {
        return new ItemRelatedFluidEntry(toFluidRef(dataset, fluidVariantId, fluid, modNames));
    }

    private List<FluidContainerEntry> toContainers(List<FluidContainerBrowserEntity> rows, DatasetSummary dataset) {
        return rows.stream()
                .map(e -> toContainer(e, dataset))
                .toList();
    }

    private FluidContainerEntry toContainer(
            FluidContainerBrowserEntity e,
            DatasetSummary dataset) {
        return new FluidContainerEntry(
                toItemRef(
                        e.getContainerItemVariantId(),
                        e.getContainerDisplayName(),
                        null,
                        assetUrlBuilder.build(dataset, e.getContainerAssetPath(), e.getContainerAssetSha256())),
                e.getAmount());
    }

    private AspectEntry toAspect(ItemAspectBrowserEntity e, DatasetSummary dataset) {
        return new AspectEntry(
                e.getAspectId(), e.getName(), e.getDescription(), e.isPrimal(), e.getAmount(),
                e.getIconItemVariantId(),
                assetUrlBuilder.build(dataset, e.getIconAssetPath(), e.getIconAssetSha256()));
    }

    private FluidBlockEntry toBlock(FluidBlockBrowserEntity e, DatasetSummary dataset) {
        return new FluidBlockEntry(toItemRef(
                e.getBlockItemVariantId(),
                e.getBlockDisplayName(),
                e.getBlockTooltipText(),
                assetUrlBuilder.build(dataset, e.getBlockAssetPath(), e.getBlockAssetSha256())));
    }

    private ItemRef toItemRef(String itemVariantId, String displayName, String tooltipText, String assetUrl) {
        return new ItemRef(itemVariantId, displayName, tooltipText, assetUrl);
    }

    private FluidRef toFluidRef(
            DatasetSummary dataset,
            String fluidVariantId,
            FluidVariantBrowserEntity fluid,
            Map<String, String> modNames) {
        return new FluidRef(
                fluidVariantId,
                fluid != null ? fluid.getFluidId() : "",
                fluid != null ? fluid.getModId() : null,
                fluid != null ? modNames.getOrDefault(fluid.getModId(), fluid.getModId()) : null,
                fluid != null ? fluid.getDisplayName() : fluidVariantId,
                fluid != null ? fluid.isGaseous() : null,
                fluid != null ? fluid.getTemperature() : null,
                fluid != null ? assetUrlBuilder.build(dataset, fluid.getAssetPath(), null) : null);
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
