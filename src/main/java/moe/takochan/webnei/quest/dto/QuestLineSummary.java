package moe.takochan.webnei.quest.dto;

public record QuestLineSummary(
        String questLineId,
        String name,
        String description,
        String visibility,
        String iconItemVariantId,
        String iconAssetUrl,
        long questCount) {
}
