package moe.takochan.webnei.model.dto;

import java.util.List;

public record RecipeDto(
        String recipeId,
        String categoryId,
        String categoryDisplayName,
        String sourcePlugin,
        String sourceModName,
        String sourceRef,
        String description,
        List<RecipeSlotDto> slots,
        GregTechRecipeDto gregtech) {
}
