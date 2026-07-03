package moe.takochan.webnei.dataset.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import moe.takochan.webnei.dataset.api.DatasetSummary;
import moe.takochan.webnei.dataset.entity.DatasetEntity;
import moe.takochan.webnei.dataset.mapper.DatasetMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DatasetService {

    private final DatasetMapper mapper;

    public DatasetService(DatasetMapper mapper) {
        this.mapper = mapper;
    }

    public List<DatasetSummary> list() {
        LambdaQueryWrapper<DatasetEntity> query = new LambdaQueryWrapper<DatasetEntity>()
                .orderByDesc(DatasetEntity::getCreatedAt)
                .orderByAsc(DatasetEntity::getPackSlug)
                .orderByAsc(DatasetEntity::getPackVersion)
                .orderByAsc(DatasetEntity::getVariant)
                .orderByAsc(DatasetEntity::getLanguage)
                .orderByAsc(DatasetEntity::getDatasetId);
        return mapper.selectList(query).stream().map(this::toSummary).toList();
    }

    private DatasetSummary toSummary(DatasetEntity entity) {
        return new DatasetSummary(
                entity.getDatasetId(),
                entity.getPackSlug(),
                entity.getPackVersion(),
                entity.getVariant(),
                entity.getLanguage(),
                entity.getDisplayName(),
                entity.getSchemaVersion(),
                entity.getExporterVersion(),
                entity.getCreatedAt(),
                entity.getMinecraftVersion());
    }
}
