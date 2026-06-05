package moe.takochan.webnei.gtore;

public record GtDimensionRef(
        String dimension,
        String fullName,
        String displayName,
        String displayAbbr,
        String iconItemVariantId,
        String iconAssetUrl,
        int sortOrder) {
}
