package moe.takochan.webnei.model.entity.view;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(QuestBrowserEntity.QuestId.class)
@Table(name = "v_quest_browser")
public class QuestBrowserEntity {
    @Id @Column(name = "dataset_id", nullable = false) private String datasetId;
    @Id @Column(name = "quest_id", nullable = false) private String questId;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String description;
    @Column(nullable = false) private String visibility;
    @Column(name = "repeat_time", nullable = false) private int repeatTime;
    @Column(name = "quest_logic", nullable = false) private String questLogic;
    @Column(name = "task_logic", nullable = false) private String taskLogic;
    @Column(name = "icon_item_variant_id", nullable = false) private String iconItemVariantId;
    @Column(name = "icon_asset_path") private String iconAssetPath;
    @Column(name = "icon_asset_sha256") private String iconAssetSha256;
    protected QuestBrowserEntity() {}
    public String getQuestId() { return questId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getVisibility() { return visibility; }
    public int getRepeatTime() { return repeatTime; }
    public String getQuestLogic() { return questLogic; }
    public String getTaskLogic() { return taskLogic; }
    public String getIconItemVariantId() { return iconItemVariantId; }
    public String getIconAssetPath() { return iconAssetPath; }
    public String getIconAssetSha256() { return iconAssetSha256; }
    public static class QuestId { private String datasetId; private String questId; public QuestId() {} public QuestId(String datasetId, String questId) { this.datasetId = datasetId; this.questId = questId; } public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof QuestId other)) return false; return datasetId.equals(other.datasetId) && questId.equals(other.questId); } public int hashCode() { return datasetId.hashCode() * 31 + questId.hashCode(); } }
}
