package moe.takochan.webnei.catalog.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import moe.takochan.webnei.asset.AssetUrlService;
import moe.takochan.webnei.catalog.api.FluidListEntry;
import moe.takochan.webnei.catalog.api.IconAsset;
import moe.takochan.webnei.catalog.api.ItemListEntry;
import moe.takochan.webnei.catalog.entity.FluidBrowserRow;
import moe.takochan.webnei.catalog.entity.ItemBrowserRow;
import moe.takochan.webnei.catalog.mapper.FluidBrowserMapper;
import moe.takochan.webnei.catalog.mapper.ItemBrowserMapper;
import moe.takochan.webnei.catalog.search.SearchQueryParser;
import moe.takochan.webnei.catalog.search.SearchTerm;
import moe.takochan.webnei.common.api.PageResponse;
import moe.takochan.webnei.dataset.entity.DatasetEntity;
import moe.takochan.webnei.dataset.mapper.DatasetMapper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private static final int DEFAULT_PAGE_SIZE = 60;
    private static final int MAX_PAGE_SIZE = 120;
    private static final String ITEM_DISPLAY_NAME_CONDITION =
            "lower(regexp_replace(display_name, '§.', '', 'g')) LIKE {0} ESCAPE '!'";
    private static final String FLUID_DISPLAY_NAME_CONDITION =
            "lower(regexp_replace(display_name, '§.', '', 'g')) LIKE {0} ESCAPE '!'";
    private static final String MOD_NAME_CONDITION =
            "lower(CASE WHEN mod_id = 'minecraft' AND mod_name IS NULL THEN 'Minecraft' ELSE coalesce(mod_name, '') END) LIKE {0} ESCAPE '!'";
    private static final String ITEM_TOOLTIP_CONDITION =
            "(CASE WHEN position(E'\\n' in tooltip_text) > 0 "
                    + "THEN lower(regexp_replace(substring(tooltip_text from position(E'\\n' in tooltip_text) + 1), '§.', '', 'g')) "
                    + "ELSE '' END) LIKE {0} ESCAPE '!'";
    private static final String ITEM_ORE_DICTIONARY_CONDITION =
            "EXISTS ("
                    + "SELECT 1 FROM ore_dictionary_entry ode "
                    + "WHERE ode.dataset_id = v_item_browser.dataset_id "
                    + "AND ode.item_variant_id = v_item_browser.item_variant_id "
                    + "AND lower(ode.dictionary_name) LIKE {0} ESCAPE '!'"
                    + ")";
    private static final String ITEM_IDENTIFIER_CONDITION =
            "(lower(item_id) LIKE {0} ESCAPE '!' "
                    + "OR EXISTS ("
                    + "SELECT 1 FROM item i "
                    + "WHERE i.dataset_id = v_item_browser.dataset_id "
                    + "AND i.item_id = v_item_browser.item_id "
                    + "AND lower(CAST(i.runtime_item_id AS text) || ':' || CAST(v_item_browser.damage AS text)) LIKE {0} ESCAPE '!'"
                    + "))";
    private static final String FLUID_IDENTIFIER_CONDITION =
            "(lower(fluid_id) LIKE {0} ESCAPE '!' OR lower(registry_name) LIKE {0} ESCAPE '!')";
    private static final String UNSUPPORTED_SEARCH_CONDITION = "1 = 0";

    private final DatasetMapper datasetMapper;
    private final ItemBrowserMapper itemBrowserMapper;
    private final FluidBrowserMapper fluidBrowserMapper;
    private final SearchQueryParser searchQueryParser;
    private final AssetUrlService assetUrlService;

    public CatalogService(
            DatasetMapper datasetMapper,
            ItemBrowserMapper itemBrowserMapper,
            FluidBrowserMapper fluidBrowserMapper,
            SearchQueryParser searchQueryParser,
            AssetUrlService assetUrlService) {
        this.datasetMapper = datasetMapper;
        this.itemBrowserMapper = itemBrowserMapper;
        this.fluidBrowserMapper = fluidBrowserMapper;
        this.searchQueryParser = searchQueryParser;
        this.assetUrlService = assetUrlService;
    }

    public PageResponse<ItemListEntry> items(String datasetId, String query, int page, int size) {
        DatasetEntity dataset = requireDataset(datasetId);
        PageRequest request = pageRequest(page, size);
        Page<ItemBrowserRow> result = itemBrowserMapper.selectPage(
                new Page<>(request.page() + 1L, request.size()),
                itemQuery(datasetId, searchQueryParser.parse(query)));
        List<ItemListEntry> items = result.getRecords().stream()
                .map(row -> toItemEntry(dataset, row))
                .toList();
        return new PageResponse<>(items, request.page(), request.size(), result.getTotal());
    }

    public PageResponse<FluidListEntry> fluids(String datasetId, String query, int page, int size) {
        DatasetEntity dataset = requireDataset(datasetId);
        PageRequest request = pageRequest(page, size);
        Page<FluidBrowserRow> result = fluidBrowserMapper.selectPage(
                new Page<>(request.page() + 1L, request.size()),
                fluidQuery(datasetId, searchQueryParser.parse(query)));
        List<FluidListEntry> items = result.getRecords().stream()
                .map(row -> toFluidEntry(dataset, row))
                .toList();
        return new PageResponse<>(items, request.page(), request.size(), result.getTotal());
    }

    private QueryWrapper<ItemBrowserRow> itemQuery(String datasetId, List<List<SearchTerm>> groups) {
        QueryWrapper<ItemBrowserRow> query = new QueryWrapper<ItemBrowserRow>()
                .eq("dataset_id", datasetId)
                .orderByAsc("list_index", "item_variant_id");
        applySearchGroups(query, groups, this::applyItemTerm);
        return query;
    }

    private QueryWrapper<FluidBrowserRow> fluidQuery(String datasetId, List<List<SearchTerm>> groups) {
        QueryWrapper<FluidBrowserRow> query = new QueryWrapper<FluidBrowserRow>()
                .eq("dataset_id", datasetId)
                .orderByAsc("display_name", "fluid_id");
        applySearchGroups(query, groups, this::applyFluidTerm);
        return query;
    }

    private void applySearchGroups(
            QueryWrapper<?> query,
            List<List<SearchTerm>> groups,
            TermConditionApplier applier) {
        if (groups.isEmpty()) {
            return;
        }
        query.and(search -> {
            boolean first = true;
            for (List<SearchTerm> group : groups) {
                if (first) {
                    search.nested(nested -> applySearchGroup(nested, group, applier));
                    first = false;
                } else {
                    search.or(nested -> applySearchGroup(nested, group, applier));
                }
            }
        });
    }

    private void applySearchGroup(QueryWrapper<?> query, List<SearchTerm> group, TermConditionApplier applier) {
        for (SearchTerm term : group) {
            applier.apply(query, term);
        }
    }

    private void applyItemTerm(QueryWrapper<?> query, SearchTerm term) {
        if (term.isText()) {
            applyPatternCondition(query, ITEM_DISPLAY_NAME_CONDITION, term);
        } else if (term.isMod()) {
            applyPatternCondition(query, MOD_NAME_CONDITION, term);
        } else if (term.isTooltip()) {
            applyPatternCondition(query, ITEM_TOOLTIP_CONDITION, term);
        } else if (term.isOre()) {
            applyPatternCondition(query, ITEM_ORE_DICTIONARY_CONDITION, term);
        } else if (term.isIdentifier()) {
            applyPatternCondition(query, ITEM_IDENTIFIER_CONDITION, term);
        }
    }

    private void applyFluidTerm(QueryWrapper<?> query, SearchTerm term) {
        if (term.isText()) {
            applyPatternCondition(query, FLUID_DISPLAY_NAME_CONDITION, term);
        } else if (term.isMod()) {
            applyPatternCondition(query, MOD_NAME_CONDITION, term);
        } else if (term.isIdentifier()) {
            applyPatternCondition(query, FLUID_IDENTIFIER_CONDITION, term);
        } else {
            applyConstantCondition(query, UNSUPPORTED_SEARCH_CONDITION, term);
        }
    }

    private static void applyPatternCondition(QueryWrapper<?> query, String condition, SearchTerm term) {
        query.apply(wrapNegation(condition, term), term.getPattern());
    }

    private static void applyConstantCondition(QueryWrapper<?> query, String condition, SearchTerm term) {
        query.apply(wrapNegation(condition, term));
    }

    private static String wrapNegation(String condition, SearchTerm term) {
        return term.isNegated() ? "NOT (" + condition + ")" : condition;
    }

    private DatasetEntity requireDataset(String datasetId) {
        DatasetEntity dataset = datasetMapper.selectById(datasetId);
        if (dataset == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found");
        }
        return dataset;
    }

    private static PageRequest pageRequest(int page, int size) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        return new PageRequest(normalizedPage, normalizedSize);
    }

    private ItemListEntry toItemEntry(DatasetEntity dataset, ItemBrowserRow row) {
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
                icon(dataset, row.getIconPath(), row.getIconWidth(), row.getIconHeight(), row.getIconMetadataJson()),
                row.getTooltipText(),
                row.getChemicalExpression());
    }

    private FluidListEntry toFluidEntry(DatasetEntity dataset, FluidBrowserRow row) {
        return new FluidListEntry(
                row.getFluidId(),
                row.getFluidId(),
                row.getDisplayName(),
                row.getModId(),
                modName(row.getModId(), row.getModName()),
                row.getRegistryName(),
                row.getChemicalExpression(),
                row.getTemperature(),
                row.isGaseous(),
                icon(dataset, row.getIconPath(), row.getIconWidth(), row.getIconHeight(), row.getIconMetadataJson()));
    }

    private IconAsset icon(DatasetEntity dataset, String path, Integer width, Integer height, String metadataJson) {
        String url = assetUrlService.assetUrl(dataset, path);
        return url == null ? null : new IconAsset(url, width, height, metadataJson);
    }

    private static String modName(String modId, String modName) {
        if (modName == null && "minecraft".equals(modId)) {
            return "Minecraft";
        }
        return modName;
    }

    @FunctionalInterface
    private interface TermConditionApplier {
        void apply(QueryWrapper<?> query, SearchTerm term);
    }

    private record PageRequest(int page, int size) {}
}
