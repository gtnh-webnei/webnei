package moe.takochan.webnei.common;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.dataset.DatasetSummary;

import org.springframework.stereotype.Service;

@Service
public class EntityRefService {

    private final ItemRefRepository itemRefRepo;
    private final FluidRefRepository fluidRefRepo;
    private final AssetUrlBuilder assetUrlBuilder;

    public EntityRefService(ItemRefRepository itemRefRepo,
                            FluidRefRepository fluidRefRepo,
                            AssetUrlBuilder assetUrlBuilder) {
        this.itemRefRepo = itemRefRepo;
        this.fluidRefRepo = fluidRefRepo;
        this.assetUrlBuilder = assetUrlBuilder;
    }

    public Map<String, ItemRef> itemRefs(DatasetSummary dataset, Collection<String> itemVariantIds) {
        String datasetId = dataset.datasetId();
        var ids = itemVariantIds.stream()
                .filter(Objects::nonNull)
                .filter(id -> !id.isBlank())
                .distinct()
                .toList();
        if (ids.isEmpty()) return Map.of();

        Map<String, ItemRef> refs = new LinkedHashMap<>();
        for (ItemRefEntity item : itemRefRepo.findByDatasetIdAndItemVariantIdIn(datasetId, ids)) {
            refs.put(item.getItemVariantId(), toItemRef(dataset, item));
        }
        return refs;
    }

    public Map<String, FluidRef> fluidRefs(DatasetSummary dataset, Collection<String> fluidVariantIds) {
        String datasetId = dataset.datasetId();
        var ids = fluidVariantIds.stream()
                .filter(Objects::nonNull)
                .filter(id -> !id.isBlank())
                .distinct()
                .toList();
        if (ids.isEmpty()) return Map.of();

        Map<String, FluidRef> refs = new LinkedHashMap<>();
        for (FluidRefEntity fluid : fluidRefRepo.findByDatasetIdAndFluidVariantIdIn(datasetId, ids)) {
            refs.put(fluid.getFluidVariantId(), toFluidRef(dataset, fluid));
        }
        return refs;
    }

    public ItemRef itemRef(DatasetSummary dataset, String itemVariantId) {
        if (itemVariantId == null || itemVariantId.isBlank()) {
            return emptyItemRef();
        }
        return itemRefs(dataset, java.util.List.of(itemVariantId))
                .getOrDefault(itemVariantId, new ItemRef(itemVariantId, null, null, itemVariantId, null, null));
    }

    public FluidRef fluidRef(DatasetSummary dataset, String fluidVariantId) {
        if (fluidVariantId == null || fluidVariantId.isBlank()) {
            return emptyFluidRef();
        }
        return fluidRefs(dataset, java.util.List.of(fluidVariantId))
                .getOrDefault(fluidVariantId, new FluidRef(fluidVariantId, "", null, null, fluidVariantId, null, null, null));
    }

    private ItemRef toItemRef(DatasetSummary dataset, ItemRefEntity item) {
        return new ItemRef(
                item.getItemVariantId(),
                item.getModId(),
                item.getModName(),
                item.getDisplayName(),
                item.getTooltipText(),
                assetUrlBuilder.build(dataset, item.getAssetPath(), item.getAssetSha256()));
    }

    private FluidRef toFluidRef(DatasetSummary dataset, FluidRefEntity fluid) {
        return new FluidRef(
                fluid.getFluidVariantId(),
                fluid.getFluidId(),
                fluid.getModId(),
                fluid.getModName(),
                fluid.getDisplayName(),
                fluid.isGaseous(),
                fluid.getTemperature(),
                assetUrlBuilder.build(dataset, fluid.getAssetPath(), null));
    }

    private static ItemRef emptyItemRef() {
        return new ItemRef("", null, null, "", null, null);
    }

    private static FluidRef emptyFluidRef() {
        return new FluidRef("", "", null, null, "", null, null, null);
    }
}
