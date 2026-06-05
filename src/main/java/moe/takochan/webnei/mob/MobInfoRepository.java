package moe.takochan.webnei.mob;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MobInfoRepository extends JpaRepository<MobInfoEntity, MobInfoEntity.MobInfoId> {
}
