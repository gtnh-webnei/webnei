package moe.takochan.webnei.quest;

import java.util.List;

import moe.takochan.webnei.common.NotFoundException;
import moe.takochan.webnei.dataset.DatasetService;
import moe.takochan.webnei.dataset.DatasetSummary;
import moe.takochan.webnei.quest.dto.QuestDetail;
import moe.takochan.webnei.quest.dto.QuestLineDetail;
import moe.takochan.webnei.quest.dto.QuestLineSummary;

import org.springframework.stereotype.Service;

@Service
public class QuestService {

    private final DatasetService datasetService;
    private final QuestDao questDao;

    public QuestService(DatasetService datasetService, QuestDao questDao) {
        this.datasetService = datasetService;
        this.questDao = questDao;
    }

    public List<QuestLineSummary> listLines(String datasetId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return questDao.listLines(dataset);
    }

    public QuestLineDetail lineDetail(String datasetId, String questLineId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return questDao.findLine(dataset, questLineId)
                .orElseThrow(() -> new NotFoundException("Quest line not found: " + questLineId));
    }

    public QuestDetail questDetail(String datasetId, String questId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return questDao.findQuest(dataset, questId)
                .orElseThrow(() -> new NotFoundException("Quest not found: " + questId));
    }
}
