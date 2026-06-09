package moe.takochan.webnei.model.dto;

import java.util.List;
import java.util.Map;

public record GregTechRecipeDto(
        String recipeKind,
        boolean visibleInNei,
        String voltageTier,
        Integer voltage,
        Integer amperage,
        int durationTicks,
        Integer specialValue,
        List<GregTechSpecialItemDto> specialItems,
        Map<String, MetadataValueDto> metadata) {
}
