package moe.takochan.webnei.model.entity.view;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(QuestLineBrowserEntity.QuestLineId.class)
@Table(name = "v_quest_line_browser")
public class QuestLineBrowserEntity {
    @Id @Column(name = "dataset_id", nullable = false) private String datasetId;
    @Id @Column(name = "quest_line_id", nullable = false) private String questLineId;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String description;
    @Column(nullable = false) private String visibility;
    @Column(name = "order_index", nullable = false) private int orderIndex;
    @Column(name = "icon_item_variant_id", nullable = false) private String iconItemVariantId;
    @Column(name = "icon_asset_path") private String iconAssetPath;
    @Column(name = "icon_asset_sha256") private String iconAssetSha256;
    @Column(name = "quest_count", nullable = false) private long questCount;
    protected QuestLineBrowserEntity() {}
    public String getQuestLineId() { return questLineId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getVisibility() { return visibility; }
    public String getIconItemVariantId() { return iconItemVariantId; }
    public String getIconAssetPath() { return iconAssetPath; }
    public String getIconAssetSha256() { return iconAssetSha256; }
    public long getQuestCount() { return questCount; }
    public static class QuestLineId { private String datasetId; private String questLineId; public QuestLineId() {} public QuestLineId(String datasetId, String questLineId) { this.datasetId = datasetId; this.questLineId = questLineId; } public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof QuestLineId other)) return false; return datasetId.equals(other.datasetId) && questLineId.equals(other.questLineId); } public int hashCode() { return datasetId.hashCode() * 31 + questLineId.hashCode(); } }
}
