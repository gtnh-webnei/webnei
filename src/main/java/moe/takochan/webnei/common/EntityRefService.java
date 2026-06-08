package moe.takochan.webnei.common;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.dataset.DatasetSummary;
import moe.takochan.webnei.fluid.FluidModOptionEntity;
import moe.takochan.webnei.fluid.FluidModOptionRepository;
import moe.takochan.webnei.fluid.FluidVariantBrowserEntity;
import moe.takochan.webnei.fluid.FluidVariantRepository;
import moe.takochan.webnei.item.ItemModOptionEntity;
import moe.takochan.webnei.item.ItemModOptionRepository;
import moe.takochan.webnei.item.ItemVariantBrowserEntity;
import moe.takochan.webnei.item.ItemVariantRepository;

import org.springframework.stereotype.Service;

@Service
public class EntityRefService {

    private final ItemVariantRepository itemVariantRepo;
    private final FluidVariantRepository fluidVariantRepo;
    private final ItemModOptionRepository itemModOptionRepo;
    private final FluidModOptionRepository fluidModOptionRepo;
    private final AssetUrlBuilder assetUrlBuilder;

    public EntityRefService(ItemVariantRepository itemVariantRepo,
                            FluidVariantRepository fluidVariantRepo,
                            ItemModOptionRepository itemModOptionRepo,
                            FluidModOptionRepository fluidModOptionRepo,
                            AssetUrlBuilder assetUrlBuilder) {
        this.itemVariantRepo = itemVariantRepo;
        this.fluidVariantRepo = fluidVariantRepo;
        this.itemModOptionRepo = itemModOptionRepo;
        this.fluidModOptionRepo = fluidModOptionRepo;
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

        Map<String, String> modNames = itemModOptionRepo.findByDatasetIdOrderByNameAscModIdAsc(datasetId).stream()
                .collect(java.util.stream.Collectors.toMap(ItemModOptionEntity::getModId, ItemModOptionEntity::getName));
        Map<String, ItemRef> refs = new LinkedHashMap<>();
        for (ItemVariantBrowserEntity item : itemVariantRepo.findByDatasetIdAndItemVariantIdIn(datasetId, ids)) {
            refs.put(item.getItemVariantId(), toItemRef(dataset, item, modNames));
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

        Map<String, String> modNames = fluidModOptionRepo.findByDatasetIdOrderByNameAscModIdAsc(datasetId).stream()
                .collect(java.util.stream.Collectors.toMap(FluidModOptionEntity::getModId, FluidModOptionEntity::getName));
        Map<String, FluidRef> refs = new LinkedHashMap<>();
        for (FluidVariantBrowserEntity fluid : fluidVariantRepo.findByDatasetIdAndFluidVariantIdIn(datasetId, ids)) {
            refs.put(fluid.getFluidVariantId(), toFluidRef(dataset, fluid, modNames));
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

    private ItemRef toItemRef(DatasetSummary dataset, ItemVariantBrowserEntity item, Map<String, String> modNames) {
        return new ItemRef(
                item.getItemVariantId(),
                item.getModId(),
                modNames.getOrDefault(item.getModId(), item.getModId()),
                item.getDisplayName(),
                item.getTooltipText(),
                assetUrlBuilder.build(dataset, item.getAssetPath(), item.getAssetSha256()));
    }

    private FluidRef toFluidRef(DatasetSummary dataset, FluidVariantBrowserEntity fluid, Map<String, String> modNames) {
        return new FluidRef(
                fluid.getFluidVariantId(),
                fluid.getFluidId(),
                fluid.getModId(),
                modNames.getOrDefault(fluid.getModId(), fluid.getModId()),
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
