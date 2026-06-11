package moe.takochan.webnei.controller;

import java.util.List;
import moe.takochan.webnei.model.dto.QuestDetail;
import moe.takochan.webnei.model.dto.QuestLineDetail;
import moe.takochan.webnei.model.dto.QuestLineSummary;
import moe.takochan.webnei.service.DatasetResolver;
import moe.takochan.webnei.service.QuestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasets/{datasetId}")
public class QuestController {

    private final DatasetResolver datasetResolver;
    private final QuestService questService;

    public QuestController(DatasetResolver datasetResolver, QuestService questService) {
        this.datasetResolver = datasetResolver;
        this.questService = questService;
    }

    @GetMapping("/quest-lines")
    public List<QuestLineSummary> listLines(@PathVariable String datasetId) {
        return questService.listLines(datasetResolver.resolve(datasetId));
    }

    @GetMapping("/quest-lines/{lineId}")
    public QuestLineDetail lineDetail(
            @PathVariable String datasetId,
            @PathVariable String lineId) {
        return questService.lineDetail(datasetResolver.resolve(datasetId), lineId);
    }

    @GetMapping("/quests/{questId}")
    public QuestDetail questDetail(
            @PathVariable String datasetId,
            @PathVariable String questId) {
        return questService.questDetail(datasetResolver.resolve(datasetId), questId);
    }

    @GetMapping("/quests/{questId}/line")
    public QuestLineSummary questLineForQuest(
            @PathVariable String datasetId,
            @PathVariable String questId) {
        return questService.questLineForQuest(datasetResolver.resolve(datasetId), questId);
    }
}
