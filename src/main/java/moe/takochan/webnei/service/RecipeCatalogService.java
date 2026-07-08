package moe.takochan.webnei.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import moe.takochan.webnei.dto.common.IconAsset;
import moe.takochan.webnei.dto.common.PageResponse;
import moe.takochan.webnei.dto.recipe.RecipeCategoryListEntry;
import moe.takochan.webnei.entity.DatasetEntity;
import moe.takochan.webnei.entity.RecipeCategoryBrowserRow;
import moe.takochan.webnei.mapper.DatasetMapper;
import moe.takochan.webnei.mapper.RecipeCategoryBrowserMapper;
import moe.takochan.webnei.search.CatalogSearchSupport;
import moe.takochan.webnei.search.SearchQueryParser;
import moe.takochan.webnei.search.SearchTermType;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class RecipeCatalogService {

    private static final int DEFAULT_PAGE_SIZE = 60;
    private static final int MAX_PAGE_SIZE = 120;

    private static final String COLUMN_DATASET_ID = "dataset_id";
    private static final String COLUMN_CATEGORY_ID = "category_id";
    private static final String COLUMN_MOD_ID = "mod_id";
    private static final String COLUMN_SEARCH_STD = "search_std";
    private static final String COLUMN_SEARCH_MOD = "search_mod";
    private static final String MOD_ID_MINECRAFT = "minecraft";

    // 默认(TEXT)搜索匹配名称+模组聚合列 search_std；@ 搜索匹配模组列 search_mod。沿用 item/fluid 的解析与搜索语义。
    private static final CatalogSearchSupport SEARCH = new CatalogSearchSupport(
            "v_recipe_category_browser",
            COLUMN_CATEGORY_ID,
            Map.of(
                    SearchTermType.TEXT, COLUMN_SEARCH_STD,
                    SearchTermType.MOD, COLUMN_SEARCH_MOD));

    private final DatasetMapper datasetMapper;
    private final RecipeCategoryBrowserMapper recipeCategoryBrowserMapper;
    private final SearchQueryParser searchQueryParser;
    private final AssetUrlService assetUrlService;

    public RecipeCatalogService(
            DatasetMapper datasetMapper,
            RecipeCategoryBrowserMapper recipeCategoryBrowserMapper,
            SearchQueryParser searchQueryParser,
            AssetUrlService assetUrlService) {
        this.datasetMapper = datasetMapper;
        this.recipeCategoryBrowserMapper = recipeCategoryBrowserMapper;
        this.searchQueryParser = searchQueryParser;
        this.assetUrlService = assetUrlService;
    }

    public PageResponse<RecipeCategoryListEntry> listCategories(String datasetId, String searchText, int page, int size) {
        DatasetEntity dataset = requireDataset(datasetId);
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        Page<RecipeCategoryBrowserRow> result = recipeCategoryBrowserMapper.selectPage(
                new Page<>(normalizedPage + 1L, normalizedSize),
                query(datasetId, searchText));
        List<RecipeCategoryListEntry> categories = result.getRecords().stream()
                .map(row -> toEntry(dataset, row))
                .toList();
        return new PageResponse<>(categories, normalizedPage, normalizedSize, result.getTotal());
    }

    private QueryWrapper<RecipeCategoryBrowserRow> query(String datasetId, String searchText) {
        // minecraft 原版分类排最前；其后按模组名分组，组内按去色码名称，category_id 兜底稳定顺序。
        // minecraft 为代码常量字面量、非用户输入，直接内联到表达式排序列，QueryWrapper 原样拼入 ORDER BY。
        QueryWrapper<RecipeCategoryBrowserRow> query = new QueryWrapper<RecipeCategoryBrowserRow>()
                .eq(COLUMN_DATASET_ID, datasetId)
                .orderByDesc("(" + COLUMN_MOD_ID + " = '" + MOD_ID_MINECRAFT + "')")
                .orderByAsc(COLUMN_SEARCH_MOD, COLUMN_SEARCH_STD, COLUMN_CATEGORY_ID);
        SEARCH.apply(query, COLUMN_CATEGORY_ID, searchQueryParser.parse(searchText));
        return query;
    }

    private DatasetEntity requireDataset(String datasetId) {
        DatasetEntity dataset = datasetMapper.selectById(datasetId);
        if (dataset == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found");
        }
        return dataset;
    }

    private static int normalizePage(int page) {
        return Math.max(page, 0);
    }

    private static int normalizeSize(int size) {
        return size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
    }

    private RecipeCategoryListEntry toEntry(DatasetEntity dataset, RecipeCategoryBrowserRow row) {
        return new RecipeCategoryListEntry(
                row.getCategoryId(),
                row.getCategoryId(),
                row.getDisplayName(),
                row.getModId(),
                row.getModName(),
                row.getRecipeCount(),
                icon(dataset, row));
    }

    private IconAsset icon(DatasetEntity dataset, RecipeCategoryBrowserRow row) {
        String url = assetUrlService.assetUrl(dataset, row.getIconPath());
        return url == null ? null : new IconAsset(url, row.getIconWidth(), row.getIconHeight(), row.getIconMetadataJson());
    }
}
