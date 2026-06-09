package moe.takochan.webnei.repository.table;

import java.util.List;
import moe.takochan.webnei.model.entity.table.QuestRewardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestRewardRepository extends JpaRepository<QuestRewardEntity, QuestRewardEntity.QuestRewardId> {
    List<QuestRewardEntity> findByDatasetIdAndQuestIdOrderByListIndexAsc(String datasetId, String questId);
}
