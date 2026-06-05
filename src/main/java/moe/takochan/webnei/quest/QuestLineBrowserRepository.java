package moe.takochan.webnei.quest;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestLineBrowserRepository extends JpaRepository<QuestLineBrowserEntity, QuestLineBrowserEntity.QuestLineId> {
    List<QuestLineBrowserEntity> findByDatasetIdOrderByOrderIndexAscQuestLineIdAsc(String datasetId);
}
