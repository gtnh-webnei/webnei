package moe.takochan.webnei.item;

import java.util.List;

import moe.takochan.webnei.extras.AspectEntry;
import moe.takochan.webnei.extras.ItemRelatedFluidEntry;

public record ItemDetailDto(
        String itemId,
        String modId,
        String modName,
        String registryName,
        String unlocalizedName,
        int maxStackSize,
        int maxDamage,
        int damage,
        String nbtText,
        String chemicalExpression,
        String displayName,
        String tooltipText,
        String assetUrl,
        Integer assetWidth,
        Integer assetHeight,
        List<ItemWorldGenerationRef> worldGeneration,
        List<String> oreDictNames,
        List<ItemRelatedFluidEntry> relatedFluids,
        List<AspectEntry> aspects) {
}
