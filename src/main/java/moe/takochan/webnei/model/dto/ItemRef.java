package moe.takochan.webnei.model.dto;

public record ItemRef(
        String itemVariantId,
        String modId,
        String modName,
        String displayName,
        String tooltipText,
        String assetUrl) {
}
