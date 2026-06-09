package moe.takochan.webnei.model.dto;

public record QuestLineSummary(
        String questLineId,
        String name,
        String description,
        String visibility,
        String iconItemVariantId,
        String iconAssetUrl,
        long questCount) {
}
