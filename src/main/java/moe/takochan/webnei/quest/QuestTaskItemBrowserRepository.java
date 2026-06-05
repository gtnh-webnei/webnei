package moe.takochan.webnei.quest;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestTaskItemBrowserRepository extends JpaRepository<QuestTaskItemBrowserEntity, QuestTaskItemBrowserEntity.QuestTaskItemId> {
    List<QuestTaskItemBrowserEntity> findByDatasetIdAndTaskIdIn(String datasetId, List<String> taskIds, Sort sort);
}
