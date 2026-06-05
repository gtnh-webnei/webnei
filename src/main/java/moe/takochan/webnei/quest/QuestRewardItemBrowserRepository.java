package moe.takochan.webnei.quest;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestRewardItemBrowserRepository extends JpaRepository<QuestRewardItemBrowserEntity, QuestRewardItemBrowserEntity.QuestRewardItemId> {
    List<QuestRewardItemBrowserEntity> findByDatasetIdAndRewardIdIn(String datasetId, List<String> rewardIds, Sort sort);
}
