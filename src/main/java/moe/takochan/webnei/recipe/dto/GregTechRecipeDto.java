package moe.takochan.webnei.recipe.dto;

import java.util.List;

public record GregTechRecipeDto(
        String voltageTier,
        long voltage,
        int amperage,
        int durationTicks,
        boolean requiresCleanroom,
        String additionalInfo,
        List<GregTechSpecialItemDto> specialItems) {
}
