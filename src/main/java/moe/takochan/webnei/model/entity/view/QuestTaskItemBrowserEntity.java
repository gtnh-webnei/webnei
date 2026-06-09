package moe.takochan.webnei.model.entity.view;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(QuestTaskItemBrowserEntity.QuestTaskItemId.class)
@Table(name = "v_quest_task_item_browser")
public class QuestTaskItemBrowserEntity {
    @Id @Column(name = "dataset_id", nullable = false) private String datasetId;
    @Id @Column(name = "task_id", nullable = false) private String taskId;
    @Id @Column(name = "list_index", nullable = false) private int listIndex;
    @Id @Column(name = "group_id", nullable = false) private String groupId;
    @Id @Column(name = "item_variant_id", nullable = false) private String itemVariantId;
    @Id @Column(name = "fluid_variant_id", nullable = false) private String fluidVariantId;
    @Id @Column(nullable = false) private int amount;
    @Column(name = "display_name") private String displayName;
    @Column(name = "mod_id") private String modId;
    @Column(name = "asset_path") private String assetPath;
    @Column(name = "asset_sha256") private String assetSha256;
    protected QuestTaskItemBrowserEntity() {}
    public String getTaskId() { return taskId; }
    public int getListIndex() { return listIndex; }
    public String getGroupId() { return groupId; }
    public String getItemVariantId() { return itemVariantId; }
    public String getFluidVariantId() { return fluidVariantId; }
    public int getAmount() { return amount; }
    public String getDisplayName() { return displayName; }
    public String getModId() { return modId; }
    public String getAssetPath() { return assetPath; }
    public String getAssetSha256() { return assetSha256; }
    public static class QuestTaskItemId { private String datasetId; private String taskId; private int listIndex; private String groupId; private String itemVariantId; private String fluidVariantId; private int amount; public QuestTaskItemId() {} public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof QuestTaskItemId other)) return false; return listIndex == other.listIndex && amount == other.amount && datasetId.equals(other.datasetId) && taskId.equals(other.taskId) && groupId.equals(other.groupId) && itemVariantId.equals(other.itemVariantId) && fluidVariantId.equals(other.fluidVariantId); } public int hashCode() { int r = datasetId.hashCode(); r = 31 * r + taskId.hashCode(); r = 31 * r + listIndex; r = 31 * r + groupId.hashCode(); r = 31 * r + itemVariantId.hashCode(); r = 31 * r + fluidVariantId.hashCode(); r = 31 * r + amount; return r; } }
}
