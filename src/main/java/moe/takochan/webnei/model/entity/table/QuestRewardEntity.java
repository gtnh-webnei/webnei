package moe.takochan.webnei.model.entity.table;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(QuestRewardEntity.QuestRewardId.class)
@Table(name = "quest_reward")
public class QuestRewardEntity {
    @Id @Column(name = "dataset_id", nullable = false) private String datasetId;
    @Id @Column(name = "reward_id", nullable = false) private String rewardId;
    @Column(name = "quest_id", nullable = false) private String questId;
    @Column(name = "list_index", nullable = false) private int listIndex;
    @Column(nullable = false) private String name;
    @Column(name = "reward_type", nullable = false) private String rewardType;
    @Column(nullable = false) private String command;
    @Column(nullable = false) private int xp;
    @Column(nullable = false) private boolean levels;
    @Column(name = "complete_quest_id", nullable = false) private String completeQuestId;
    protected QuestRewardEntity() {}
    public String getRewardId() { return rewardId; }
    public int getListIndex() { return listIndex; }
    public String getName() { return name; }
    public String getRewardType() { return rewardType; }
    public String getCommand() { return command; }
    public int getXp() { return xp; }
    public boolean isLevels() { return levels; }
    public String getCompleteQuestId() { return completeQuestId; }
    public static class QuestRewardId { private String datasetId; private String rewardId; public QuestRewardId() {} public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof QuestRewardId other)) return false; return datasetId.equals(other.datasetId) && rewardId.equals(other.rewardId); } public int hashCode() { return datasetId.hashCode() * 31 + rewardId.hashCode(); } }
}
