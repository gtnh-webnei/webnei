package moe.takochan.webnei.model.dto;

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
        long applicableItemCount,
        String modName,
        String handlerClass) {
}
