package moe.takochan.webnei.model.dto;

import java.util.List;

public record QuestLineDetail(
        QuestLineSummary line,
        List<QuestNode> nodes,
        List<QuestEdge> edges) {
}
