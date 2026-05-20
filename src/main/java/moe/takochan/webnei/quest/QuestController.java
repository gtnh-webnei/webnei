package moe.takochan.webnei.quest;

import java.util.List;

import moe.takochan.webnei.quest.dto.QuestDetail;
import moe.takochan.webnei.quest.dto.QuestLineDetail;
import moe.takochan.webnei.quest.dto.QuestLineSummary;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasets/{datasetId}")
public class QuestController {

    private final QuestService questService;

    public QuestController(QuestService questService) {
        this.questService = questService;
    }

    @GetMapping("/quest-lines")
    public List<QuestLineSummary> listLines(@PathVariable String datasetId) {
        return questService.listLines(datasetId);
    }

    @GetMapping("/quest-lines/{lineId}")
    public QuestLineDetail lineDetail(
            @PathVariable String datasetId,
            @PathVariable String lineId) {
        return questService.lineDetail(datasetId, lineId);
    }

    @GetMapping("/quests/{questId}")
    public QuestDetail questDetail(
            @PathVariable String datasetId,
            @PathVariable String questId) {
        return questService.questDetail(datasetId, questId);
    }
}
