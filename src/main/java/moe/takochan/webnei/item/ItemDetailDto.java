package moe.takochan.webnei.item;

public record ItemDetailDto(
        String itemVariantId,
        String itemId,
        String modId,
        String modName,
        String registryName,
        String unlocalizedName,
        int maxStackSize,
        int maxDamage,
        int damage,
        String nbtHash,
        String nbtText,
        String chemicalExpression,
        String displayName,
        String tooltipText,
        String assetUrl,
        Integer assetWidth,
        Integer assetHeight) {
}
