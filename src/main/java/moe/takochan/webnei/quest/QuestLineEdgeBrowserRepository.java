package moe.takochan.webnei.quest;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestLineEdgeBrowserRepository extends JpaRepository<QuestLineEdgeBrowserEntity, QuestLineEdgeBrowserEntity.QuestLineEdgeId> {
    List<QuestLineEdgeBrowserEntity> findByDatasetIdAndQuestLineId(String datasetId, String questLineId);
}
