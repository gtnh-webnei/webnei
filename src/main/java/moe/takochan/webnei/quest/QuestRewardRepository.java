package moe.takochan.webnei.quest;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestRewardRepository extends JpaRepository<QuestRewardEntity, QuestRewardEntity.QuestRewardId> {
    List<QuestRewardEntity> findByDatasetIdAndQuestIdOrderByListIndexAsc(String datasetId, String questId);
}
