package moe.takochan.webnei.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import moe.takochan.webnei.dto.aspect.AspectBrief;
import moe.takochan.webnei.dto.aspect.AspectDetail;
import moe.takochan.webnei.dto.aspect.AspectItemListEntry;
import moe.takochan.webnei.dto.aspect.AspectListEntry;
import moe.takochan.webnei.dto.common.IconAsset;
import moe.takochan.webnei.dto.common.PageResponse;
import moe.takochan.webnei.entity.AspectCatalogRow;
import moe.takochan.webnei.entity.AspectComponentRow;
import moe.takochan.webnei.entity.AspectItemBrowserRow;
import moe.takochan.webnei.entity.DatasetEntity;
import moe.takochan.webnei.mapper.AspectBrowserMapper;
import moe.takochan.webnei.mapper.AspectComponentBrowserMapper;
import moe.takochan.webnei.mapper.AspectItemBrowserMapper;
import moe.takochan.webnei.mapper.DatasetMapper;
import moe.takochan.webnei.search.CatalogSearchSupport;
import moe.takochan.webnei.search.SearchQueryParser;
import moe.takochan.webnei.search.SearchTermType;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class AspectCatalogService {

    private static final int DEFAULT_PAGE_SIZE = 60;
    private static final int MAX_PAGE_SIZE = 120;

    private static final CatalogSearchSupport ITEM_SEARCH = new CatalogSearchSupport(
            "mv_item_search",
            "item_variant_id",
            Map.of(
                    SearchTermType.TEXT, "s_std",
                    SearchTermType.MOD, "s_mod",
                    SearchTermType.TOOLTIP, "s_tooltip",
                    SearchTermType.ORE_DICTIONARY, "s_ore",
                    SearchTermType.IDENTIFIER, "s_id"));

    private final DatasetMapper datasetMapper;
    private final AspectBrowserMapper aspectBrowserMapper;
    private final AspectComponentBrowserMapper aspectComponentBrowserMapper;
    private final AspectItemBrowserMapper aspectItemBrowserMapper;
    private final SearchQueryParser searchQueryParser;
    private final AssetUrlService assetUrlService;

    public AspectCatalogService(
            DatasetMapper datasetMapper,
            AspectBrowserMapper aspectBrowserMapper,
            AspectComponentBrowserMapper aspectComponentBrowserMapper,
            AspectItemBrowserMapper aspectItemBrowserMapper,
            SearchQueryParser searchQueryParser,
            AssetUrlService assetUrlService) {
        this.datasetMapper = datasetMapper;
        this.aspectBrowserMapper = aspectBrowserMapper;
        this.aspectComponentBrowserMapper = aspectComponentBrowserMapper;
        this.aspectItemBrowserMapper = aspectItemBrowserMapper;
        this.searchQueryParser = searchQueryParser;
        this.assetUrlService = assetUrlService;
    }

    public List<AspectListEntry> list(String datasetId) {
        DatasetEntity dataset = requireDataset(datasetId);
        List<AspectCatalogRow> aspects = aspectBrowserMapper.selectList(
                new QueryWrapper<AspectCatalogRow>()
                        .eq("dataset_id", datasetId)
                        .orderByAsc("registry_order", "aspect_id"));
        if (aspects.isEmpty()) {
            return List.of();
        }

        Map<String, List<AspectBrief>> components = groupComponents(
                dataset,
                aspectComponentBrowserMapper.selectList(
                        new QueryWrapper<AspectComponentRow>()
                                .eq("dataset_id", datasetId)
                                .orderByAsc("parent_aspect_id", "component_index")));
        return aspects.stream()
                .map(row -> toListEntry(dataset, row, components.getOrDefault(row.getAspectId(), List.of())))
                .toList();
    }

    public AspectDetail detail(String datasetId, String aspectId) {
        DatasetEntity dataset = requireDataset(datasetId);
        AspectCatalogRow aspect = requireAspect(datasetId, aspectId);
        List<AspectBrief> components = aspectComponentBrowserMapper.selectList(
                        new QueryWrapper<AspectComponentRow>()
                                .eq("dataset_id", datasetId)
                                .eq("parent_aspect_id", aspectId)
                                .orderByAsc("component_index"))
                .stream()
                .map(row -> toBrief(dataset, row))
                .toList();
        List<AspectBrief> usedBy = usedBy(dataset, aspectId);
        return new AspectDetail(
                aspect.getAspectId(),
                aspect.getDisplayName(),
                aspect.getDescription(),
                aspect.isPrimal(),
                aspect.getColor(),
                icon(dataset, aspect),
                components,
                usedBy);
    }

    public PageResponse<AspectItemListEntry> listItems(
            String datasetId,
            String aspectId,
            String searchText,
            int page,
            int size) {
        DatasetEntity dataset = requireDataset(datasetId);
        requireAspect(datasetId, aspectId);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = normalizeSize(size);
        Page<AspectItemBrowserRow> result = aspectItemBrowserMapper.selectPage(
                new Page<>(normalizedPage + 1L, normalizedSize),
                itemQuery(datasetId, aspectId, searchText));
        List<AspectItemListEntry> items = result.getRecords().stream()
                .map(row -> toItemEntry(dataset, row))
                .toList();
        return new PageResponse<>(items, normalizedPage, normalizedSize, result.getTotal());
    }

    private QueryWrapper<AspectItemBrowserRow> itemQuery(
            String datasetId,
            String aspectId,
            String searchText) {
        QueryWrapper<AspectItemBrowserRow> query = new QueryWrapper<AspectItemBrowserRow>()
                .eq("dataset_id", datasetId)
                .eq("aspect_id", aspectId);
        ITEM_SEARCH.apply(
                query,
                "item_variant_id",
                searchQueryParser.parse(searchText));
        return query.orderByAsc("list_index", "item_variant_id");
    }

    private List<AspectBrief> usedBy(DatasetEntity dataset, String aspectId) {
        List<AspectComponentRow> edges = aspectComponentBrowserMapper.selectList(
                new QueryWrapper<AspectComponentRow>()
                        .eq("dataset_id", dataset.getDatasetId())
                        .eq("aspect_id", aspectId));
        Set<String> parentIds = new LinkedHashSet<>();
        for (AspectComponentRow edge : edges) {
            parentIds.add(edge.getParentAspectId());
        }
        if (parentIds.isEmpty()) {
            return List.of();
        }
        return aspectBrowserMapper.selectList(
                        new QueryWrapper<AspectCatalogRow>()
                                .eq("dataset_id", dataset.getDatasetId())
                                .in("aspect_id", parentIds)
                                .orderByAsc("registry_order", "aspect_id"))
                .stream()
                .map(row -> toBrief(dataset, row))
                .toList();
    }

    private DatasetEntity requireDataset(String datasetId) {
        DatasetEntity dataset = datasetMapper.selectById(datasetId);
        if (dataset == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found");
        }
        return dataset;
    }

    private AspectCatalogRow requireAspect(String datasetId, String aspectId) {
        AspectCatalogRow aspect = aspectBrowserMapper.selectOne(
                new QueryWrapper<AspectCatalogRow>()
                        .eq("dataset_id", datasetId)
                        .eq("aspect_id", aspectId));
        if (aspect == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aspect not found");
        }
        return aspect;
    }

    private static int normalizeSize(int size) {
        return size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
    }

    private Map<String, List<AspectBrief>> groupComponents(
            DatasetEntity dataset,
            List<AspectComponentRow> rows) {
        Map<String, List<AspectBrief>> result = new HashMap<>();
        for (AspectComponentRow row : rows) {
            result.computeIfAbsent(row.getParentAspectId(), ignored -> new ArrayList<>())
                    .add(toBrief(dataset, row));
        }
        return result;
    }

    private AspectListEntry toListEntry(
            DatasetEntity dataset,
            AspectCatalogRow row,
            List<AspectBrief> components) {
        return new AspectListEntry(
                row.getAspectId(),
                row.getDisplayName(),
                row.getDescription(),
                row.isPrimal(),
                row.getColor(),
                icon(dataset, row),
                List.copyOf(components),
                row.getAssociatedItemCount());
    }

    private AspectBrief toBrief(DatasetEntity dataset, AspectComponentRow row) {
        return new AspectBrief(
                row.getAspectId(),
                row.getDisplayName(),
                row.getColor(),
                icon(dataset, row));
    }

    private AspectBrief toBrief(DatasetEntity dataset, AspectCatalogRow row) {
        return new AspectBrief(
                row.getAspectId(),
                row.getDisplayName(),
                row.getColor(),
                icon(dataset, row));
    }

    private AspectItemListEntry toItemEntry(DatasetEntity dataset, AspectItemBrowserRow row) {
        return new AspectItemListEntry(
                row.getItemVariantId(),
                row.getDisplayName(),
                row.getModId(),
                row.getModName(),
                row.getRegistryName(),
                row.getAmount(),
                icon(dataset, row));
    }

    private IconAsset icon(DatasetEntity dataset, AspectCatalogRow row) {
        return icon(dataset, row.getIconPath(), row.getIconWidth(), row.getIconHeight(), row.getIconMetadataJson());
    }

    private IconAsset icon(DatasetEntity dataset, AspectComponentRow row) {
        return icon(dataset, row.getIconPath(), row.getIconWidth(), row.getIconHeight(), row.getIconMetadataJson());
    }

    private IconAsset icon(DatasetEntity dataset, AspectItemBrowserRow row) {
        return icon(dataset, row.getIconPath(), row.getIconWidth(), row.getIconHeight(), row.getIconMetadataJson());
    }

    private IconAsset icon(
            DatasetEntity dataset,
            String path,
            Integer width,
            Integer height,
            String metadataJson) {
        String url = assetUrlService.assetUrl(dataset, path);
        return url == null ? null : new IconAsset(url, width, height, metadataJson);
    }
}
