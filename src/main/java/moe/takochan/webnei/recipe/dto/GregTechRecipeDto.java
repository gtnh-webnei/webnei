package moe.takochan.webnei.recipe.dto;

import java.util.List;
import java.util.Map;

public record GregTechRecipeDto(
        String recipeKind,
        boolean visibleInNei,
        String voltageTier,
        Integer voltage,
        Integer amperage,
        int durationTicks,
        boolean requiresCleanroom,
        boolean requiresLowGravity,
        Integer specialValue,
        Integer fuelValue,
        String additionalInfo,
        List<GregTechSpecialItemDto> specialItems,
        Map<String, MetadataValueDto> metadata,
        GregTechFuelProfileDto fuelProfile) {
}
