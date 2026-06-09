package moe.takochan.webnei.repository.view;

import java.util.List;
import moe.takochan.webnei.model.entity.view.QuestLineBrowserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestLineBrowserRepository extends JpaRepository<QuestLineBrowserEntity, QuestLineBrowserEntity.QuestLineId> {
    List<QuestLineBrowserEntity> findByDatasetIdOrderByOrderIndexAscQuestLineIdAsc(String datasetId);
}
