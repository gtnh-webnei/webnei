package moe.takochan.webnei.recipe.dto;

public record RecipeSlotCandidateDto(
        String itemVariantId,
        String fluidVariantId,
        int amount,
        String displayName,
        String modId,
        String assetUrl) {
}
