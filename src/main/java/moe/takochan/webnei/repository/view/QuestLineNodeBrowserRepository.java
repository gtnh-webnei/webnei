package moe.takochan.webnei.repository.view;

import java.util.List;
import moe.takochan.webnei.model.entity.view.QuestLineNodeBrowserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestLineNodeBrowserRepository extends JpaRepository<QuestLineNodeBrowserEntity, QuestLineNodeBrowserEntity.QuestLineNodeId> {
    List<QuestLineNodeBrowserEntity> findByDatasetIdAndQuestLineIdOrderByPosYAscPosXAsc(String datasetId, String questLineId);
}
