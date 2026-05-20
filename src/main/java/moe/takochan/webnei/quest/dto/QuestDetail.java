package moe.takochan.webnei.quest.dto;

import java.util.List;

public record QuestDetail(
        QuestNode node,
        List<QuestTaskDetail> tasks,
        List<QuestRewardDetail> rewards,
        String questLogic,
        String taskLogic) {
}
