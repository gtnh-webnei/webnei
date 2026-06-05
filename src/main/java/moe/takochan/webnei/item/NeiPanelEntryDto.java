package moe.takochan.webnei.item;

public record NeiPanelEntryDto(
        String itemVariantId,
        String itemId,
        String modId,
        String modName,
        String registryName,
        int damage,
        String nbtHash,
        String displayName,
        String assetUrl,
        int panelIndex) {
}
