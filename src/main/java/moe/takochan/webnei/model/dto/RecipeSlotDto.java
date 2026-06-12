package moe.takochan.webnei.model.dto;

import java.util.List;
import java.util.Map;

public record RecipeSlotDto(
        String role,
        int slotIndex,
        String itemVariantId,
        String fluidVariantId,
        int amount,
        double probability,
        String groupId,
        String displayName,
        String modId,
        String modName,
        Boolean fluidGaseous,
        Integer fluidTemperature,
        String tooltipText,
        String assetUrl,
        List<RecipeSlotCandidateDto> candidates,
        String placement,
        String slotGroupKey,
        int slotGroupOrder,
        Map<String, MetadataValueDto> metadata) {
}
