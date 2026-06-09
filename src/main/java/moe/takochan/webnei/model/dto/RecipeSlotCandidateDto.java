package moe.takochan.webnei.model.dto;

public record RecipeSlotCandidateDto(
        String itemVariantId,
        String fluidVariantId,
        int amount,
        String displayName,
        String modId,
        String modName,
        Boolean fluidGaseous,
        Integer fluidTemperature,
        String tooltipText,
        String assetUrl) {
}
