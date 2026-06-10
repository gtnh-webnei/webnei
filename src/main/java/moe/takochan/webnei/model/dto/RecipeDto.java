package moe.takochan.webnei.model.dto;

import java.util.List;
import java.util.Map;

public record RecipeDto(
        String recipeId,
        String categoryId,
        String categoryDisplayName,
        String sourcePlugin,
        String sourceModName,
        String sourceRef,
        String description,
        List<RecipeSlotDto> slots,
        Map<String, MetadataValueDto> metadata) {
}
