package moe.takochan.webnei.quest;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(QuestLineEdgeBrowserEntity.QuestLineEdgeId.class)
@Table(name = "v_quest_line_edge_browser")
public class QuestLineEdgeBrowserEntity {
    @Id @Column(name = "dataset_id", nullable = false) private String datasetId;
    @Id @Column(name = "quest_line_id", nullable = false) private String questLineId;
    @Id @Column(name = "quest_id", nullable = false) private String questId;
    @Id @Column(name = "required_quest_id", nullable = false) private String requiredQuestId;
    protected QuestLineEdgeBrowserEntity() {}
    public String getQuestId() { return questId; }
    public String getRequiredQuestId() { return requiredQuestId; }
    public static class QuestLineEdgeId { private String datasetId; private String questLineId; private String questId; private String requiredQuestId; public QuestLineEdgeId() {} public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof QuestLineEdgeId other)) return false; return datasetId.equals(other.datasetId) && questLineId.equals(other.questLineId) && questId.equals(other.questId) && requiredQuestId.equals(other.requiredQuestId); } public int hashCode() { int r = datasetId.hashCode(); r = 31 * r + questLineId.hashCode(); r = 31 * r + questId.hashCode(); r = 31 * r + requiredQuestId.hashCode(); return r; } }
}
