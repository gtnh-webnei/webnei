package moe.takochan.webnei.model.dto;

import java.util.List;

public record QuestTaskItemEntry(
        String itemVariantId,
        String fluidVariantId,
        int amount,
        String displayName,
        String modId,
        String assetUrl) {

    public record Group(int listIndex, String groupId, List<QuestTaskItemEntry> entries) {}
}
