package moe.takochan.webnei.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import moe.takochan.webnei.dto.common.IconAsset;
import moe.takochan.webnei.dto.common.PageResponse;
import moe.takochan.webnei.dto.item.ItemListEntry;
import moe.takochan.webnei.entity.DatasetEntity;
import moe.takochan.webnei.entity.ItemBrowserRow;
import moe.takochan.webnei.mapper.DatasetMapper;
import moe.takochan.webnei.mapper.ItemBrowserMapper;
import moe.takochan.webnei.search.CatalogSearchSupport;
import moe.takochan.webnei.search.SearchQueryParser;
import moe.takochan.webnei.search.SearchTermType;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class ItemCatalogService {

    private static final int DEFAULT_PAGE_SIZE = 60;
    private static final int MAX_PAGE_SIZE = 120;

    private static final CatalogSearchSupport SEARCH = new CatalogSearchSupport(
            "mv_item_search",
            "item_variant_id",
            Map.of(
                    SearchTermType.TEXT, "s_std",
                    SearchTermType.MOD, "s_mod",
                    SearchTermType.TOOLTIP, "s_tooltip",
                    SearchTermType.ORE_DICTIONARY, "s_ore",
                    SearchTermType.IDENTIFIER, "s_id"));

    private final DatasetMapper datasetMapper;
    private final ItemBrowserMapper itemBrowserMapper;
    private final SearchQueryParser searchQueryParser;
    private final AssetUrlService assetUrlService;

    public ItemCatalogService(
            DatasetMapper datasetMapper,
            ItemBrowserMapper itemBrowserMapper,
            SearchQueryParser searchQueryParser,
            AssetUrlService assetUrlService) {
        this.datasetMapper = datasetMapper;
        this.itemBrowserMapper = itemBrowserMapper;
        this.searchQueryParser = searchQueryParser;
        this.assetUrlService = assetUrlService;
    }

    public PageResponse<ItemListEntry> list(String datasetId, String searchText, int page, int size) {
        DatasetEntity dataset = requireDataset(datasetId);
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        Page<ItemBrowserRow> result = itemBrowserMapper.selectPage(
                new Page<>(normalizedPage + 1L, normalizedSize),
                query(datasetId, searchText));
        List<ItemListEntry> items = result.getRecords().stream()
                .map(row -> toEntry(dataset, row))
                .toList();
        return new PageResponse<>(items, normalizedPage, normalizedSize, result.getTotal());
    }

    private QueryWrapper<ItemBrowserRow> query(String datasetId, String searchText) {
        QueryWrapper<ItemBrowserRow> query = new QueryWrapper<ItemBrowserRow>()
                .eq("dataset_id", datasetId)
                .orderByAsc("list_index", "item_variant_id");
        SEARCH.apply(query, "item_variant_id", searchQueryParser.parse(searchText));
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

    private ItemListEntry toEntry(DatasetEntity dataset, ItemBrowserRow row) {
        return new ItemListEntry(
                row.getItemVariantId(),
                row.getItemVariantId(),
                row.getItemId(),
                row.getDisplayName(),
                row.getModId(),
                modName(row.getModId(), row.getModName()),
                row.getRegistryName(),
                row.getDamage(),
                row.getListIndex(),
                icon(dataset, row),
                row.getTooltipText(),
                row.getChemicalExpression());
    }

    private IconAsset icon(DatasetEntity dataset, ItemBrowserRow row) {
        String url = assetUrlService.assetUrl(dataset, row.getIconPath());
        return url == null ? null : new IconAsset(url, row.getIconWidth(), row.getIconHeight(), row.getIconMetadataJson());
    }

    private static String modName(String modId, String modName) {
        if (modName == null && "minecraft".equals(modId)) {
            return "Minecraft";
        }
        return modName;
    }
}
