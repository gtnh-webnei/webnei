package moe.takochan.webnei.repository.table;

import moe.takochan.webnei.model.entity.table.DatasetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetRepository extends JpaRepository<DatasetEntity, String> {
}
