package moe.takochan.webnei.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import moe.takochan.webnei.config.DatasetProperties;
import moe.takochan.webnei.dto.dataset.DatasetListEntry;
import moe.takochan.webnei.dto.dataset.DatasetListResponse;
import moe.takochan.webnei.entity.DatasetEntity;
import moe.takochan.webnei.mapper.DatasetMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DatasetService {

    private final DatasetMapper mapper;
    private final DatasetProperties properties;

    public DatasetService(DatasetMapper mapper, DatasetProperties properties) {
        this.mapper = mapper;
        this.properties = properties;
    }

    public DatasetListResponse list() {
        LambdaQueryWrapper<DatasetEntity> query = new LambdaQueryWrapper<DatasetEntity>()
                .orderByDesc(DatasetEntity::getCreatedAt)
                .orderByAsc(DatasetEntity::getPackSlug)
                .orderByAsc(DatasetEntity::getPackVersion)
                .orderByAsc(DatasetEntity::getVariant)
                .orderByAsc(DatasetEntity::getLanguage)
                .orderByAsc(DatasetEntity::getDatasetId);
        List<DatasetListEntry> items = mapper.selectList(query).stream().map(this::toEntry).toList();
        String defaultId = properties.getDefaultId();
        return new DatasetListResponse(defaultId == null || defaultId.isBlank() ? null : defaultId, items);
    }

    private DatasetListEntry toEntry(DatasetEntity entity) {
        return new DatasetListEntry(
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
