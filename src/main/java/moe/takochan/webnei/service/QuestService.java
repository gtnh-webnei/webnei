package moe.takochan.webnei.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.NotFoundException;
import moe.takochan.webnei.model.dto.DatasetSummary;
import moe.takochan.webnei.model.dto.QuestDetail;
import moe.takochan.webnei.model.dto.QuestEdge;
import moe.takochan.webnei.model.dto.QuestLineDetail;
import moe.takochan.webnei.model.dto.QuestLineSummary;
import moe.takochan.webnei.model.dto.QuestNode;
import moe.takochan.webnei.model.dto.QuestRewardDetail;
import moe.takochan.webnei.model.dto.QuestTaskDetail;
import moe.takochan.webnei.model.dto.QuestTaskItemEntry;
import moe.takochan.webnei.model.entity.table.QuestRewardEntity;
import moe.takochan.webnei.model.entity.table.QuestTaskEntity;
import moe.takochan.webnei.model.entity.view.QuestBrowserEntity;
import moe.takochan.webnei.model.entity.view.QuestLineBrowserEntity;
import moe.takochan.webnei.model.entity.view.QuestLineNodeBrowserEntity;
import moe.takochan.webnei.repository.table.QuestRewardRepository;
import moe.takochan.webnei.repository.table.QuestTaskRepository;
import moe.takochan.webnei.repository.view.QuestBrowserRepository;
import moe.takochan.webnei.repository.view.QuestLineBrowserRepository;
import moe.takochan.webnei.repository.view.QuestLineEdgeBrowserRepository;
import moe.takochan.webnei.repository.view.QuestLineNodeBrowserRepository;
import moe.takochan.webnei.repository.view.QuestRewardItemBrowserRepository;
import moe.takochan.webnei.repository.view.QuestTaskItemBrowserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class QuestService {

    private final QuestLineBrowserRepository lineRepo;
    private final QuestBrowserRepository questRepo;
    private final QuestLineNodeBrowserRepository nodeRepo;
    private final QuestLineEdgeBrowserRepository edgeRepo;
    private final QuestTaskRepository taskRepo;
    private final QuestRewardRepository rewardRepo;
    private final QuestTaskItemBrowserRepository taskItemRepo;
    private final QuestRewardItemBrowserRepository rewardItemRepo;
    private final AssetUrlBuilder assetUrlBuilder;

    public QuestService(QuestLineBrowserRepository lineRepo,
                        QuestBrowserRepository questRepo,
                        QuestLineNodeBrowserRepository nodeRepo,
                        QuestLineEdgeBrowserRepository edgeRepo,
                        QuestTaskRepository taskRepo,
                        QuestRewardRepository rewardRepo,
                        QuestTaskItemBrowserRepository taskItemRepo,
                        QuestRewardItemBrowserRepository rewardItemRepo,
                        AssetUrlBuilder assetUrlBuilder) {
        this.lineRepo = lineRepo;
        this.questRepo = questRepo;
        this.nodeRepo = nodeRepo;
        this.edgeRepo = edgeRepo;
        this.taskRepo = taskRepo;
        this.rewardRepo = rewardRepo;
        this.taskItemRepo = taskItemRepo;
        this.rewardItemRepo = rewardItemRepo;
        this.assetUrlBuilder = assetUrlBuilder;
    }

    public List<QuestLineSummary> listLines(DatasetSummary dataset) {
        String datasetId = dataset.datasetId();
        return lineRepo.findByDatasetIdOrderByOrderIndexAscQuestLineIdAsc(datasetId)
                .stream()
                .map(e -> toLineSummary(e, dataset))
                .toList();
    }

    public QuestLineDetail lineDetail(DatasetSummary dataset, String questLineId) {
        String datasetId = dataset.datasetId();
        QuestLineBrowserEntity line = lineRepo.findById(new QuestLineBrowserEntity.QuestLineId(datasetId, questLineId))
                .orElseThrow(() -> new NotFoundException("Quest line not found: " + questLineId));
        List<QuestNode> nodes = nodeRepo.findByDatasetIdAndQuestLineIdOrderByPosYAscPosXAsc(datasetId, questLineId)
                .stream()
                .map(e -> toNode(e, dataset))
                .toList();
        List<QuestEdge> edges = edgeRepo.findByDatasetIdAndQuestLineId(datasetId, questLineId)
                .stream()
                .map(e -> new QuestEdge(e.getQuestId(), e.getRequiredQuestId()))
                .toList();
        return new QuestLineDetail(toLineSummary(line, dataset), nodes, edges);
    }

    public QuestDetail questDetail(DatasetSummary dataset, String questId) {
        String datasetId = dataset.datasetId();
        QuestBrowserEntity quest = questRepo.findById(new QuestBrowserEntity.QuestId(datasetId, questId))
                .orElseThrow(() -> new NotFoundException("Quest not found: " + questId));
        List<QuestTaskDetail> tasks = loadTasks(dataset, questId);
        List<QuestRewardDetail> rewards = loadRewards(dataset, questId);
        return new QuestDetail(toNode(quest, dataset), tasks, rewards, quest.getQuestLogic(), quest.getTaskLogic());
    }

    private List<QuestTaskDetail> loadTasks(DatasetSummary dataset, String questId) {
        List<QuestTaskEntity> tasks = taskRepo.findByDatasetIdAndQuestIdOrderByListIndexAsc(dataset.datasetId(), questId);
        if (tasks.isEmpty()) return List.of();
        List<String> taskIds = tasks.stream().map(QuestTaskEntity::getTaskId).toList();
        Map<String, List<QuestTaskItemEntry.Group>> itemGroups = groupTaskItems(dataset, taskIds);
        return tasks.stream()
                .map(t -> new QuestTaskDetail(
                        t.getTaskId(), t.getListIndex(), t.getName(), t.getTaskType(), t.isConsume(),
                        t.getNumberRequired(), t.getMobVariantId(), t.getDimensionName(),
                        itemGroups.getOrDefault(t.getTaskId(), List.of())))
                .toList();
    }

    private List<QuestRewardDetail> loadRewards(DatasetSummary dataset, String questId) {
        List<QuestRewardEntity> rewards = rewardRepo.findByDatasetIdAndQuestIdOrderByListIndexAsc(dataset.datasetId(), questId);
        if (rewards.isEmpty()) return List.of();
        List<String> rewardIds = rewards.stream().map(QuestRewardEntity::getRewardId).toList();
        Map<String, List<QuestTaskItemEntry.Group>> itemGroups = groupRewardItems(dataset, rewardIds);
        return rewards.stream()
                .map(r -> new QuestRewardDetail(
                        r.getRewardId(), r.getListIndex(), r.getName(), r.getRewardType(),
                        r.getCommand(), r.getXp(), r.isLevels(), r.getCompleteQuestId(),
                        itemGroups.getOrDefault(r.getRewardId(), List.of())))
                .toList();
    }

    private Map<String, List<QuestTaskItemEntry.Group>> groupTaskItems(DatasetSummary dataset, List<String> taskIds) {
        List<GroupRow> rows = taskItemRepo.findByDatasetIdAndTaskIdIn(
                        dataset.datasetId(), taskIds, Sort.by("taskId").ascending().and(Sort.by("listIndex").ascending()))
                .stream()
                .map(e -> new GroupRow(e.getTaskId(), e.getListIndex(), e.getGroupId(),
                        e.getItemVariantId(), e.getFluidVariantId(), e.getAmount(),
                        e.getDisplayName(), e.getModId(), e.getAssetPath(), e.getAssetSha256()))
                .toList();
        return groupAndMapRows(rows, dataset);
    }

    private Map<String, List<QuestTaskItemEntry.Group>> groupRewardItems(DatasetSummary dataset, List<String> rewardIds) {
        List<GroupRow> rows = rewardItemRepo.findByDatasetIdAndRewardIdIn(
                        dataset.datasetId(), rewardIds, Sort.by("rewardId").ascending().and(Sort.by("listIndex").ascending()))
                .stream()
                .map(e -> new GroupRow(e.getRewardId(), e.getListIndex(), e.getGroupId(),
                        e.getItemVariantId(), e.getFluidVariantId(), e.getAmount(),
                        e.getDisplayName(), e.getModId(), e.getAssetPath(), e.getAssetSha256()))
                .toList();
        return groupAndMapRows(rows, dataset);
    }

    private Map<String, List<QuestTaskItemEntry.Group>> groupAndMapRows(List<GroupRow> rows, DatasetSummary dataset) {
        if (rows.isEmpty()) return Map.of();
        Map<String, Map<Integer, QuestTaskItemEntry.Group>> nested = new LinkedHashMap<>();
        for (GroupRow r : rows) {
            Map<Integer, QuestTaskItemEntry.Group> byIndex = nested.computeIfAbsent(r.ownerId(), k -> new LinkedHashMap<>());
            QuestTaskItemEntry.Group group = byIndex.get(r.listIndex());
            if (group == null) {
                group = new QuestTaskItemEntry.Group(r.listIndex(), r.groupId(), new ArrayList<>());
                byIndex.put(r.listIndex(), group);
            }
            String item = nullIfEmpty(r.itemVariantId());
            String fluid = nullIfEmpty(r.fluidVariantId());
            if (item == null && fluid == null) continue;
            group.entries().add(new QuestTaskItemEntry(
                    item, fluid, r.amount(), r.displayName(), r.modId(),
                    assetUrlBuilder.build(dataset, r.assetPath(), r.assetSha())));
        }
        Map<String, List<QuestTaskItemEntry.Group>> out = new LinkedHashMap<>();
        for (Map.Entry<String, Map<Integer, QuestTaskItemEntry.Group>> e : nested.entrySet()) {
            out.put(e.getKey(), new ArrayList<>(e.getValue().values()));
        }
        return out;
    }

    private QuestLineSummary toLineSummary(QuestLineBrowserEntity e, DatasetSummary dataset) {
        return new QuestLineSummary(
                e.getQuestLineId(), e.getName(), e.getDescription(), e.getVisibility(),
                e.getIconItemVariantId(), assetUrlBuilder.build(dataset, e.getIconAssetPath(), e.getIconAssetSha256()),
                e.getQuestCount());
    }

    private QuestNode toNode(QuestBrowserEntity e, DatasetSummary dataset) {
        return new QuestNode(
                e.getQuestId(), e.getName(), e.getDescription(), e.getVisibility(), e.getRepeatTime(),
                e.getIconItemVariantId(), assetUrlBuilder.build(dataset, e.getIconAssetPath(), e.getIconAssetSha256()),
                0, 0, 0, 0);
    }

    private QuestNode toNode(QuestLineNodeBrowserEntity e, DatasetSummary dataset) {
        return new QuestNode(
                e.getQuestId(), e.getName(), e.getDescription(), e.getVisibility(), e.getRepeatTime(),
                e.getIconItemVariantId(), assetUrlBuilder.build(dataset, e.getIconAssetPath(), e.getIconAssetSha256()),
                e.getPosX(), e.getPosY(), e.getSizeX(), e.getSizeY());
    }

    private record GroupRow(String ownerId, int listIndex, String groupId,
                            String itemVariantId, String fluidVariantId, int amount,
                            String displayName, String modId, String assetPath, String assetSha) {}

    private static String nullIfEmpty(String s) {
        return s == null || s.isEmpty() ? null : s;
    }
}
