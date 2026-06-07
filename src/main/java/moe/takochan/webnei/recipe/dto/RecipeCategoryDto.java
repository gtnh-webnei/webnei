package moe.takochan.webnei.recipe.dto;

public record RecipeCategoryDto(
        String categoryId,
        String handlerId,
        String displayName,
        boolean shapeless,
        String iconAssetUrl,
        int itemInputWidth,
        int itemInputHeight,
        int fluidInputWidth,
        int fluidInputHeight,
        int itemOutputWidth,
        int itemOutputHeight,
        int fluidOutputWidth,
        int fluidOutputHeight,
        long recipeCount,
        long machineCount,
        String modName,
        String handlerClass) {
}
