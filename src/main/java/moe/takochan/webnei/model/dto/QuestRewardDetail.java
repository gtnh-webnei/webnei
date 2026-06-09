package moe.takochan.webnei.model.dto;

import java.util.List;

public record QuestRewardDetail(
        String rewardId,
        int listIndex,
        String name,
        String rewardType,
        String command,
        int xp,
        boolean levels,
        String completeQuestId,
        List<QuestTaskItemEntry.Group> itemGroups) {
}
