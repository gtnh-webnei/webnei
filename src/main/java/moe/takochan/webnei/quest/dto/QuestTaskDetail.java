package moe.takochan.webnei.quest.dto;

import java.util.List;

public record QuestTaskDetail(
        String taskId,
        int listIndex,
        String name,
        String taskType,
        boolean consume,
        int numberRequired,
        String mobVariantId,
        String dimensionName,
        List<QuestTaskItemEntry.Group> itemGroups) {
}
