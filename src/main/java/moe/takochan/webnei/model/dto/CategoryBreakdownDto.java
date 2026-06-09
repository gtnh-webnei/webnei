package moe.takochan.webnei.model.dto;

public record CategoryBreakdownDto(
        String categoryId,
        String displayName,
        String iconAssetUrl,
        long count) {
}
