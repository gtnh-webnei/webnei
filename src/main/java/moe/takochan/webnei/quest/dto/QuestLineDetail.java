package moe.takochan.webnei.quest.dto;

import java.util.List;

public record QuestLineDetail(
        QuestLineSummary line,
        List<QuestNode> nodes,
        List<QuestEdge> edges) {
}
