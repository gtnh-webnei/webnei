package moe.takochan.webnei.model.entity.view;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(QuestLineNodeBrowserEntity.QuestLineNodeId.class)
@Table(name = "v_quest_line_node_browser")
public class QuestLineNodeBrowserEntity {
    @Id @Column(name = "dataset_id", nullable = false) private String datasetId;
    @Id @Column(name = "quest_line_id", nullable = false) private String questLineId;
    @Id @Column(name = "quest_id", nullable = false) private String questId;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String description;
    @Column(nullable = false) private String visibility;
    @Column(name = "repeat_time", nullable = false) private int repeatTime;
    @Column(name = "icon_item_variant_id", nullable = false) private String iconItemVariantId;
    @Column(name = "icon_asset_path") private String iconAssetPath;
    @Column(name = "icon_asset_sha256") private String iconAssetSha256;
    @Column(name = "pos_x", nullable = false) private int posX;
    @Column(name = "pos_y", nullable = false) private int posY;
    @Column(name = "size_x", nullable = false) private int sizeX;
    @Column(name = "size_y", nullable = false) private int sizeY;
    protected QuestLineNodeBrowserEntity() {}
    public String getQuestId() { return questId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getVisibility() { return visibility; }
    public int getRepeatTime() { return repeatTime; }
    public String getIconItemVariantId() { return iconItemVariantId; }
    public String getIconAssetPath() { return iconAssetPath; }
    public String getIconAssetSha256() { return iconAssetSha256; }
    public int getPosX() { return posX; }
    public int getPosY() { return posY; }
    public int getSizeX() { return sizeX; }
    public int getSizeY() { return sizeY; }
    public static class QuestLineNodeId { private String datasetId; private String questLineId; private String questId; public QuestLineNodeId() {} public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof QuestLineNodeId other)) return false; return datasetId.equals(other.datasetId) && questLineId.equals(other.questLineId) && questId.equals(other.questId); } public int hashCode() { int r = datasetId.hashCode(); r = 31 * r + questLineId.hashCode(); r = 31 * r + questId.hashCode(); return r; } }
}
