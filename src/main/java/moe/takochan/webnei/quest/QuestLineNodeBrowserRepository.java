package moe.takochan.webnei.quest;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestLineNodeBrowserRepository extends JpaRepository<QuestLineNodeBrowserEntity, QuestLineNodeBrowserEntity.QuestLineNodeId> {
    List<QuestLineNodeBrowserEntity> findByDatasetIdAndQuestLineIdOrderByPosYAscPosXAsc(String datasetId, String questLineId);
}
