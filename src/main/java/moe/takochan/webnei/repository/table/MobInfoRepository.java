package moe.takochan.webnei.repository.table;

import moe.takochan.webnei.model.entity.table.MobInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MobInfoRepository extends JpaRepository<MobInfoEntity, MobInfoEntity.MobInfoId> {
}
