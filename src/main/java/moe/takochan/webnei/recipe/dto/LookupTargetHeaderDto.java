package moe.takochan.webnei.recipe.dto;

public record LookupTargetHeaderDto(
        String targetType,
        String targetId,
        String displayName,
        String assetUrl,
        String modId,
        String modName,
        Boolean gaseous) {
}
