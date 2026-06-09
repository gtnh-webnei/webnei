package moe.takochan.webnei.repository.table;

import java.util.List;
import moe.takochan.webnei.model.entity.table.QuestTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestTaskRepository extends JpaRepository<QuestTaskEntity, QuestTaskEntity.QuestTaskId> {
    List<QuestTaskEntity> findByDatasetIdAndQuestIdOrderByListIndexAsc(String datasetId, String questId);
}
