package moe.takochan.webnei.recipe.dto;

import java.util.List;

public record RecipeDto(
        String recipeId,
        String categoryId,
        String categoryDisplayName,
        String uiKind,
        String uiTemplateId,
        String sourcePlugin,
        String sourceRef,
        String description,
        List<RecipeSlotDto> slots,
        List<SlotLayoutDto> layout,
        Integer canvasWidth,
        Integer canvasHeight,
        String backgroundAssetUrl,
        GregTechRecipeDto gregtech) {
}
