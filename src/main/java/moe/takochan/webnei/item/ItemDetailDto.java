package moe.takochan.webnei.item;

import java.util.List;

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
        List<ItemWorldGenerationRef> worldGeneration) {
}
