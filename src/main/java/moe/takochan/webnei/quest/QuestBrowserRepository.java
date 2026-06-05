package moe.takochan.webnei.quest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestBrowserRepository extends JpaRepository<QuestBrowserEntity, QuestBrowserEntity.QuestId> {
}
