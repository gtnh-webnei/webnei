package moe.takochan.webnei.recipe.dto;

import java.util.List;

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
        String tooltipText,
        String assetUrl,
        List<RecipeSlotCandidateDto> candidates,
        String placement) {
}
