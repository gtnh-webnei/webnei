package moe.takochan.webnei.model.entity.table;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(QuestTaskEntity.QuestTaskId.class)
@Table(name = "quest_task")
public class QuestTaskEntity {
    @Id @Column(name = "dataset_id", nullable = false) private String datasetId;
    @Id @Column(name = "task_id", nullable = false) private String taskId;
    @Column(name = "quest_id", nullable = false) private String questId;
    @Column(name = "list_index", nullable = false) private int listIndex;
    @Column(nullable = false) private String name;
    @Column(name = "task_type", nullable = false) private String taskType;
    @Column(nullable = false) private boolean consume;
    @Column(name = "mob_variant_id", nullable = false) private String mobVariantId;
    @Column(name = "number_required", nullable = false) private int numberRequired;
    @Column(name = "dimension_name", nullable = false) private String dimensionName;
    protected QuestTaskEntity() {}
    public String getTaskId() { return taskId; }
    public int getListIndex() { return listIndex; }
    public String getName() { return name; }
    public String getTaskType() { return taskType; }
    public boolean isConsume() { return consume; }
    public String getMobVariantId() { return mobVariantId; }
    public int getNumberRequired() { return numberRequired; }
    public String getDimensionName() { return dimensionName; }
    public static class QuestTaskId { private String datasetId; private String taskId; public QuestTaskId() {} public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof QuestTaskId other)) return false; return datasetId.equals(other.datasetId) && taskId.equals(other.taskId); } public int hashCode() { return datasetId.hashCode() * 31 + taskId.hashCode(); } }
}
