package moe.takochan.webnei.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import moe.takochan.webnei.dto.mod.ModListEntry;
import moe.takochan.webnei.entity.DatasetEntity;
import moe.takochan.webnei.entity.ModEntity;
import moe.takochan.webnei.mapper.DatasetMapper;
import moe.takochan.webnei.mapper.ModMapper;
import moe.takochan.webnei.search.SearchQueryParser;
import moe.takochan.webnei.search.SearchTerm;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class ModCatalogService {

    private final DatasetMapper datasetMapper;
    private final ModMapper modMapper;
    private final SearchQueryParser searchQueryParser;

    public ModCatalogService(DatasetMapper datasetMapper, ModMapper modMapper, SearchQueryParser searchQueryParser) {
        this.datasetMapper = datasetMapper;
        this.modMapper = modMapper;
        this.searchQueryParser = searchQueryParser;
    }

    /** 返回数据集下全部模组（不分页）；有搜索词时按名称/ID 过滤。 */
    public List<ModListEntry> list(String datasetId, String searchText) {
        requireDataset(datasetId);
        return modMapper.selectList(query(datasetId, searchText)).stream()
            .map(this::toEntry)
            .toList();
    }

    // 复用统一的搜索语法解析（| 分组、"" 精确、- 取反）；模组只在名称和 mod_id 两个可见字段上匹配。
    private QueryWrapper<ModEntity> query(String datasetId, String searchText) {
        QueryWrapper<ModEntity> query = new QueryWrapper<ModEntity>()
            .eq("dataset_id", datasetId)
            .orderByAsc("name", "mod_id");
        applySearch(query, searchQueryParser.parse(searchText));
        return query;
    }

    private static void applySearch(QueryWrapper<ModEntity> query, List<List<SearchTerm>> searchGroups) {
        if (searchGroups.isEmpty()) {
            return;
        }
        // 组间 OR、组内 AND；每个词在 name 与 mod_id 上做子串匹配，取反用 NOT。
        query.and(outer -> {
            for (int g = 0; g < searchGroups.size(); g++) {
                List<SearchTerm> group = searchGroups.get(g);
                if (g > 0) {
                    outer.or();
                }
                outer.and(groupWrapper -> {
                    for (SearchTerm term : group) {
                        String clause = "(lower(name) LIKE {0} ESCAPE '!' OR lower(mod_id) LIKE {1} ESCAPE '!')";
                        if (term.isNegated()) {
                            groupWrapper.apply("NOT " + clause, term.getPattern(), term.getPattern());
                        } else {
                            groupWrapper.apply(clause, term.getPattern(), term.getPattern());
                        }
                    }
                });
            }
        });
    }

    private DatasetEntity requireDataset(String datasetId) {
        DatasetEntity dataset = datasetMapper.selectById(datasetId);
        if (dataset == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found");
        }
        return dataset;
    }

    private ModListEntry toEntry(ModEntity entity) {
        return new ModListEntry(
            entity.getModId(),
            entity.getModId(),
            entity.getName(),
            entity.getVersion());
    }
}
