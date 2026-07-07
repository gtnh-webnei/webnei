package moe.takochan.webnei.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import moe.takochan.webnei.dto.common.IconAsset;
import moe.takochan.webnei.dto.common.PageResponse;
import moe.takochan.webnei.dto.fluid.FluidListEntry;
import moe.takochan.webnei.entity.DatasetEntity;
import moe.takochan.webnei.entity.FluidBrowserRow;
import moe.takochan.webnei.mapper.DatasetMapper;
import moe.takochan.webnei.mapper.FluidBrowserMapper;
import moe.takochan.webnei.search.CatalogSearchSupport;
import moe.takochan.webnei.search.SearchQueryParser;
import moe.takochan.webnei.search.SearchTermType;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class FluidCatalogService {

    private static final int DEFAULT_PAGE_SIZE = 60;
    private static final int MAX_PAGE_SIZE = 120;

    private static final CatalogSearchSupport SEARCH = new CatalogSearchSupport(
            "mv_fluid_search",
            "fluid_id",
            Map.of(
                    SearchTermType.TEXT, "s_std",
                    SearchTermType.MOD, "s_mod",
                    SearchTermType.IDENTIFIER, "s_id"));

    private final DatasetMapper datasetMapper;
    private final FluidBrowserMapper fluidBrowserMapper;
    private final SearchQueryParser searchQueryParser;
    private final AssetUrlService assetUrlService;

    public FluidCatalogService(
            DatasetMapper datasetMapper,
            FluidBrowserMapper fluidBrowserMapper,
            SearchQueryParser searchQueryParser,
            AssetUrlService assetUrlService) {
        this.datasetMapper = datasetMapper;
        this.fluidBrowserMapper = fluidBrowserMapper;
        this.searchQueryParser = searchQueryParser;
        this.assetUrlService = assetUrlService;
    }

    public PageResponse<FluidListEntry> list(String datasetId, String searchText, int page, int size) {
        DatasetEntity dataset = requireDataset(datasetId);
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        Page<FluidBrowserRow> result = fluidBrowserMapper.selectPage(
                new Page<>(normalizedPage + 1L, normalizedSize),
                query(datasetId, searchText));
        List<FluidListEntry> fluids = result.getRecords().stream()
                .map(row -> toEntry(dataset, row))
                .toList();
        return new PageResponse<>(fluids, normalizedPage, normalizedSize, result.getTotal());
    }

    private QueryWrapper<FluidBrowserRow> query(String datasetId, String searchText) {
        QueryWrapper<FluidBrowserRow> query = new QueryWrapper<FluidBrowserRow>()
                .eq("dataset_id", datasetId)
                .orderByAsc("display_name", "fluid_id");
        SEARCH.apply(query, "fluid_id", searchQueryParser.parse(searchText));
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

    private FluidListEntry toEntry(DatasetEntity dataset, FluidBrowserRow row) {
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
                icon(dataset, row));
    }

    private IconAsset icon(DatasetEntity dataset, FluidBrowserRow row) {
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
