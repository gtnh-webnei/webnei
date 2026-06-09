package moe.takochan.webnei.model.dto;

import java.util.List;

import moe.takochan.webnei.model.dto.AspectEntry;
import moe.takochan.webnei.model.dto.ItemRelatedFluidEntry;

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
        List<ItemWorldGenerationRef> worldGeneration,
        List<String> oreDictNames,
        List<ItemRelatedFluidEntry> relatedFluids,
        List<AspectEntry> aspects) {
}
