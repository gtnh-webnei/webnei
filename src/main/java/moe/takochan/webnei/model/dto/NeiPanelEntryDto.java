package moe.takochan.webnei.model.dto;

public record NeiPanelEntryDto(
        String itemVariantId,
        String itemId,
        String modId,
        String modName,
        String registryName,
        int damage,
        String displayName,
        String assetUrl,
        int panelIndex) {
}
