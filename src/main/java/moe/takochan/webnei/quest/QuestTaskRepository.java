package moe.takochan.webnei.quest;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestTaskRepository extends JpaRepository<QuestTaskEntity, QuestTaskEntity.QuestTaskId> {
    List<QuestTaskEntity> findByDatasetIdAndQuestIdOrderByListIndexAsc(String datasetId, String questId);
}
