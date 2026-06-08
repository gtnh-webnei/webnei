package moe.takochan.webnei.common;

public record ItemRef(
        String itemVariantId,
        String modId,
        String modName,
        String displayName,
        String tooltipText,
        String assetUrl) {
}
