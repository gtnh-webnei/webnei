package moe.takochan.webnei.catalog.api;

public record ItemListEntry(
        String id,
        String itemVariantId,
        String itemId,
        String displayName,
        String modId,
        String modName,
        String registryName,
        int damage,
        int listIndex,
        IconAsset icon,
        String tooltipText,
        String chemicalExpression) {}
