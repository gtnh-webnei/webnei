package moe.takochan.webnei.recipe.dto;

public record CategoryBreakdownDto(
        String categoryId,
        String displayName,
        String iconAssetUrl,
        long count) {
}
