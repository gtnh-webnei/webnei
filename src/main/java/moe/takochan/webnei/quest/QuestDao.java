package moe.takochan.webnei.quest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.dataset.DatasetSummary;
import moe.takochan.webnei.quest.dto.QuestDetail;
import moe.takochan.webnei.quest.dto.QuestEdge;
import moe.takochan.webnei.quest.dto.QuestLineDetail;
import moe.takochan.webnei.quest.dto.QuestLineSummary;
import moe.takochan.webnei.quest.dto.QuestNode;
import moe.takochan.webnei.quest.dto.QuestRewardDetail;
import moe.takochan.webnei.quest.dto.QuestTaskDetail;
import moe.takochan.webnei.quest.dto.QuestTaskItemEntry;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class QuestDao {

    private final JdbcClient jdbc;
    private final AssetUrlBuilder assetUrlBuilder;

    public QuestDao(JdbcClient jdbc, AssetUrlBuilder assetUrlBuilder) {
        this.jdbc = jdbc;
        this.assetUrlBuilder = assetUrlBuilder;
    }

    public List<QuestLineSummary> listLines(DatasetSummary dataset) {
        return jdbc.sql("""
                        SELECT ql.quest_line_id,
                               ql.name,
                               ql.description,
                               ql.visibility,
                               ql.icon_item_variant_id,
                               iv.asset_path AS icon_asset_path,
                               iv.asset_sha256 AS icon_asset_sha,
                               (SELECT COUNT(*) FROM quest_line_entry e
                                  WHERE e.dataset_id = ql.dataset_id
                                    AND e.quest_line_id = ql.quest_line_id) AS quest_count
                        FROM quest_line ql
                        LEFT JOIN v_item_variant_browser iv
                          ON iv.dataset_id = ql.dataset_id
                         AND iv.item_variant_id = ql.icon_item_variant_id
                        WHERE ql.dataset_id = :datasetId
                        ORDER BY ql.order_index, ql.quest_line_id
                        """)
                .param("datasetId", dataset.datasetId())
                .query((rs, n) -> new QuestLineSummary(
                        rs.getString("quest_line_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("visibility"),
                        rs.getString("icon_item_variant_id"),
                        buildAsset(dataset, rs, "icon_asset_path", "icon_asset_sha"),
                        rs.getLong("quest_count")))
                .list();
    }

    public Optional<QuestLineDetail> findLine(DatasetSummary dataset, String questLineId) {
        Optional<QuestLineSummary> head = jdbc.sql("""
                        SELECT ql.quest_line_id, ql.name, ql.description, ql.visibility,
                               ql.icon_item_variant_id,
                               iv.asset_path AS icon_asset_path,
                               iv.asset_sha256 AS icon_asset_sha,
                               (SELECT COUNT(*) FROM quest_line_entry e
                                  WHERE e.dataset_id = ql.dataset_id
                                    AND e.quest_line_id = ql.quest_line_id) AS quest_count
                        FROM quest_line ql
                        LEFT JOIN v_item_variant_browser iv
                          ON iv.dataset_id = ql.dataset_id
                         AND iv.item_variant_id = ql.icon_item_variant_id
                        WHERE ql.dataset_id = :datasetId
                          AND ql.quest_line_id = :lineId
                        """)
                .param("datasetId", dataset.datasetId())
                .param("lineId", questLineId)
                .query((rs, n) -> new QuestLineSummary(
                        rs.getString("quest_line_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("visibility"),
                        rs.getString("icon_item_variant_id"),
                        buildAsset(dataset, rs, "icon_asset_path", "icon_asset_sha"),
                        rs.getLong("quest_count")))
                .optional();
        if (head.isEmpty()) return Optional.empty();

        List<QuestNode> nodes = jdbc.sql("""
                        SELECT q.quest_id,
                               q.name,
                               q.description,
                               q.visibility,
                               q.repeat_time,
                               q.icon_item_variant_id,
                               iv.asset_path AS icon_asset_path,
                               iv.asset_sha256 AS icon_asset_sha,
                               e.pos_x, e.pos_y, e.size_x, e.size_y
                        FROM quest_line_entry e
                        JOIN quest q
                          ON q.dataset_id = e.dataset_id AND q.quest_id = e.quest_id
                        LEFT JOIN v_item_variant_browser iv
                          ON iv.dataset_id = q.dataset_id
                         AND iv.item_variant_id = q.icon_item_variant_id
                        WHERE e.dataset_id = :datasetId
                          AND e.quest_line_id = :lineId
                        ORDER BY e.pos_y, e.pos_x
                        """)
                .param("datasetId", dataset.datasetId())
                .param("lineId", questLineId)
                .query((rs, n) -> new QuestNode(
                        rs.getString("quest_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("visibility"),
                        rs.getInt("repeat_time"),
                        rs.getString("icon_item_variant_id"),
                        buildAsset(dataset, rs, "icon_asset_path", "icon_asset_sha"),
                        rs.getInt("pos_x"),
                        rs.getInt("pos_y"),
                        rs.getInt("size_x"),
                        rs.getInt("size_y")))
                .list();

        List<QuestEdge> edges = jdbc.sql("""
                        SELECT d.quest_id, d.required_quest_id
                        FROM quest_dependency d
                        WHERE d.dataset_id = :datasetId
                          AND d.quest_id IN (
                            SELECT quest_id FROM quest_line_entry
                            WHERE dataset_id = :datasetId AND quest_line_id = :lineId
                          )
                          AND d.required_quest_id IN (
                            SELECT quest_id FROM quest_line_entry
                            WHERE dataset_id = :datasetId AND quest_line_id = :lineId
                          )
                        """)
                .param("datasetId", dataset.datasetId())
                .param("lineId", questLineId)
                .query((rs, n) -> new QuestEdge(
                        rs.getString("quest_id"),
                        rs.getString("required_quest_id")))
                .list();

        return Optional.of(new QuestLineDetail(head.get(), nodes, edges));
    }

    public Optional<QuestDetail> findQuest(DatasetSummary dataset, String questId) {
        Optional<QuestNode> nodeOpt = jdbc.sql("""
                        SELECT q.quest_id, q.name, q.description, q.visibility, q.repeat_time,
                               q.icon_item_variant_id,
                               iv.asset_path AS icon_asset_path,
                               iv.asset_sha256 AS icon_asset_sha
                        FROM quest q
                        LEFT JOIN v_item_variant_browser iv
                          ON iv.dataset_id = q.dataset_id
                         AND iv.item_variant_id = q.icon_item_variant_id
                        WHERE q.dataset_id = :datasetId AND q.quest_id = :questId
                        """)
                .param("datasetId", dataset.datasetId())
                .param("questId", questId)
                .query((rs, n) -> new QuestNode(
                        rs.getString("quest_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("visibility"),
                        rs.getInt("repeat_time"),
                        rs.getString("icon_item_variant_id"),
                        buildAsset(dataset, rs, "icon_asset_path", "icon_asset_sha"),
                        0, 0, 0, 0))
                .optional();
        if (nodeOpt.isEmpty()) return Optional.empty();

        record QuestRow(String questId, String questLogic, String taskLogic) {}
        QuestRow logicRow = jdbc.sql("""
                        SELECT quest_id, quest_logic, task_logic
                        FROM quest WHERE dataset_id = :datasetId AND quest_id = :questId
                        """)
                .param("datasetId", dataset.datasetId())
                .param("questId", questId)
                .query((rs, n) -> new QuestRow(
                        rs.getString("quest_id"),
                        rs.getString("quest_logic"),
                        rs.getString("task_logic")))
                .single();

        // Tasks
        List<QuestTaskDetail> tasks = loadTasks(dataset, questId);
        List<QuestRewardDetail> rewards = loadRewards(dataset, questId);

        return Optional.of(new QuestDetail(nodeOpt.get(), tasks, rewards, logicRow.questLogic(), logicRow.taskLogic()));
    }

    private List<QuestTaskDetail> loadTasks(DatasetSummary dataset, String questId) {
        record TaskRow(String taskId, int listIndex, String name, String taskType, boolean consume,
                       int numberRequired, String mobVariantId, String dimensionName) {}
        List<TaskRow> rows = jdbc.sql("""
                        SELECT task_id, list_index, name, task_type, consume,
                               number_required, mob_variant_id, dimension_name
                        FROM quest_task
                        WHERE dataset_id = :datasetId AND quest_id = :questId
                        ORDER BY list_index
                        """)
                .param("datasetId", dataset.datasetId())
                .param("questId", questId)
                .query((rs, n) -> new TaskRow(
                        rs.getString("task_id"),
                        rs.getInt("list_index"),
                        rs.getString("name"),
                        rs.getString("task_type"),
                        rs.getBoolean("consume"),
                        rs.getInt("number_required"),
                        rs.getString("mob_variant_id"),
                        rs.getString("dimension_name")))
                .list();
        if (rows.isEmpty()) return List.of();

        List<String> taskIds = rows.stream().map(TaskRow::taskId).toList();
        Map<String, List<QuestTaskItemEntry.Group>> taskItems = loadTaskItemGroups(dataset, taskIds);

        List<QuestTaskDetail> out = new ArrayList<>(rows.size());
        for (TaskRow r : rows) {
            out.add(new QuestTaskDetail(
                    r.taskId(), r.listIndex(), r.name(), r.taskType(), r.consume(),
                    r.numberRequired(), r.mobVariantId(), r.dimensionName(),
                    taskItems.getOrDefault(r.taskId(), List.of())));
        }
        return out;
    }

    private List<QuestRewardDetail> loadRewards(DatasetSummary dataset, String questId) {
        record RewardRow(String rewardId, int listIndex, String name, String rewardType,
                         String command, int xp, boolean levels, String completeQuestId) {}
        List<RewardRow> rows = jdbc.sql("""
                        SELECT reward_id, list_index, name, reward_type, command, xp, levels, complete_quest_id
                        FROM quest_reward
                        WHERE dataset_id = :datasetId AND quest_id = :questId
                        ORDER BY list_index
                        """)
                .param("datasetId", dataset.datasetId())
                .param("questId", questId)
                .query((rs, n) -> new RewardRow(
                        rs.getString("reward_id"),
                        rs.getInt("list_index"),
                        rs.getString("name"),
                        rs.getString("reward_type"),
                        rs.getString("command"),
                        rs.getInt("xp"),
                        rs.getBoolean("levels"),
                        rs.getString("complete_quest_id")))
                .list();
        if (rows.isEmpty()) return List.of();

        List<String> rewardIds = rows.stream().map(RewardRow::rewardId).toList();
        Map<String, List<QuestTaskItemEntry.Group>> rewardItems = loadRewardItemGroups(dataset, rewardIds);

        List<QuestRewardDetail> out = new ArrayList<>(rows.size());
        for (RewardRow r : rows) {
            out.add(new QuestRewardDetail(
                    r.rewardId(), r.listIndex(), r.name(), r.rewardType(),
                    r.command(), r.xp(), r.levels(), r.completeQuestId(),
                    rewardItems.getOrDefault(r.rewardId(), List.of())));
        }
        return out;
    }

    private Map<String, List<QuestTaskItemEntry.Group>> loadTaskItemGroups(
            DatasetSummary dataset, List<String> taskIds) {
        if (taskIds.isEmpty()) return Map.of();

        List<GroupRow> rows = jdbc.sql("""
                        SELECT qi.task_id AS owner_id, qi.list_index, qi.group_id,
                               ie.item_variant_id, ie.fluid_variant_id, ie.amount,
                               COALESCE(iv.display_name, fv.display_name) AS display_name,
                               COALESCE(iv.mod_id, fv.mod_id) AS mod_id,
                               COALESCE(iv.asset_path, fv.asset_path) AS asset_path,
                               COALESCE(iv.asset_sha256, NULL) AS asset_sha
                        FROM quest_task_item qi
                        LEFT JOIN ingredient_entry ie
                          ON ie.dataset_id = qi.dataset_id AND ie.group_id = qi.group_id
                        LEFT JOIN v_item_variant_browser iv
                          ON iv.dataset_id = ie.dataset_id AND iv.item_variant_id = ie.item_variant_id
                        LEFT JOIN v_fluid_variant_browser fv
                          ON fv.dataset_id = ie.dataset_id AND fv.fluid_variant_id = ie.fluid_variant_id
                        WHERE qi.dataset_id = :datasetId
                          AND qi.task_id IN (:ids)
                        ORDER BY qi.task_id, qi.list_index
                        """)
                .param("datasetId", dataset.datasetId())
                .param("ids", taskIds)
                .query((rs, n) -> new GroupRow(
                        rs.getString("owner_id"),
                        rs.getInt("list_index"),
                        rs.getString("group_id"),
                        rs.getString("item_variant_id"),
                        rs.getString("fluid_variant_id"),
                        rs.getInt("amount"),
                        rs.getString("display_name"),
                        rs.getString("mod_id"),
                        rs.getString("asset_path"),
                        rs.getString("asset_sha")))
                .list();

        return groupAndMapRows(rows, dataset);
    }

    private Map<String, List<QuestTaskItemEntry.Group>> loadRewardItemGroups(
            DatasetSummary dataset, List<String> rewardIds) {
        if (rewardIds.isEmpty()) return Map.of();

        List<GroupRow> rows = jdbc.sql("""
                        SELECT qi.reward_id AS owner_id, qi.list_index, qi.group_id,
                               ie.item_variant_id, ie.fluid_variant_id, ie.amount,
                               COALESCE(iv.display_name, fv.display_name) AS display_name,
                               COALESCE(iv.mod_id, fv.mod_id) AS mod_id,
                               COALESCE(iv.asset_path, fv.asset_path) AS asset_path,
                               COALESCE(iv.asset_sha256, NULL) AS asset_sha
                        FROM quest_reward_item qi
                        LEFT JOIN ingredient_entry ie
                          ON ie.dataset_id = qi.dataset_id AND ie.group_id = qi.group_id
                        LEFT JOIN v_item_variant_browser iv
                          ON iv.dataset_id = ie.dataset_id AND iv.item_variant_id = ie.item_variant_id
                        LEFT JOIN v_fluid_variant_browser fv
                          ON fv.dataset_id = ie.dataset_id AND fv.fluid_variant_id = ie.fluid_variant_id
                        WHERE qi.dataset_id = :datasetId
                          AND qi.reward_id IN (:ids)
                        ORDER BY qi.reward_id, qi.list_index
                        """)
                .param("datasetId", dataset.datasetId())
                .param("ids", rewardIds)
                .query((rs, n) -> new GroupRow(
                        rs.getString("owner_id"),
                        rs.getInt("list_index"),
                        rs.getString("group_id"),
                        rs.getString("item_variant_id"),
                        rs.getString("fluid_variant_id"),
                        rs.getInt("amount"),
                        rs.getString("display_name"),
                        rs.getString("mod_id"),
                        rs.getString("asset_path"),
                        rs.getString("asset_sha")))
                .list();

        return groupAndMapRows(rows, dataset);
    }

    private Map<String, List<QuestTaskItemEntry.Group>> groupAndMapRows(
            List<GroupRow> rows, DatasetSummary dataset) {
        if (rows.isEmpty()) return Map.of();

        Map<String, Map<Integer, QuestTaskItemEntry.Group>> nested = new LinkedHashMap<>();
        for (GroupRow r : rows) {
            Map<Integer, QuestTaskItemEntry.Group> byIndex =
                    nested.computeIfAbsent(r.ownerId(), k -> new LinkedHashMap<>());
            QuestTaskItemEntry.Group group = byIndex.get(r.listIndex());
            if (group == null) {
                group = new QuestTaskItemEntry.Group(r.listIndex(), r.groupId(), new ArrayList<>());
                byIndex.put(r.listIndex(), group);
            }
            String item = nullIfEmpty(r.itemVariantId());
            String fluid = nullIfEmpty(r.fluidVariantId());
            if (item == null && fluid == null) continue;
            String url = assetUrlBuilder.build(dataset, r.assetPath(), r.assetSha());
            group.entries().add(new QuestTaskItemEntry(
                    item, fluid, r.amount(), r.displayName(), r.modId(), url));
        }

        Map<String, List<QuestTaskItemEntry.Group>> out = new LinkedHashMap<>();
        for (Map.Entry<String, Map<Integer, QuestTaskItemEntry.Group>> e : nested.entrySet()) {
            out.put(e.getKey(), new ArrayList<>(e.getValue().values()));
        }
        return out;
    }

    private record GroupRow(String ownerId, int listIndex, String groupId,
                            String itemVariantId, String fluidVariantId, int amount,
                            String displayName, String modId, String assetPath, String assetSha) {}


    private String buildAsset(DatasetSummary dataset, ResultSet rs, String pathCol, String shaCol) {
        try {
            return assetUrlBuilder.build(dataset, rs.getString(pathCol), rs.getString(shaCol));
        } catch (SQLException ex) {
            throw new IllegalStateException("read asset cols", ex);
        }
    }

    private static String nullIfEmpty(String s) {
        return s == null || s.isEmpty() ? null : s;
    }
}
