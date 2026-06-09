package moe.takochan.webnei.model.dto;

public record GtDimensionRef(
        String dimension,
        String fullName,
        String displayName,
        String displayAbbr,
        String iconItemVariantId,
        String iconAssetUrl,
        int sortOrder) {
}
