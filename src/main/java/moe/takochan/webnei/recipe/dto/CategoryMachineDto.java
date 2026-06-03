package moe.takochan.webnei.recipe.dto;

public record CategoryMachineDto(
        String itemVariantId,
        String displayName,
        String assetUrl,
        String role,
        int displayOrder) {
}
