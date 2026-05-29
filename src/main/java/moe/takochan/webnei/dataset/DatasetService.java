package moe.takochan.webnei.dataset;

import java.util.List;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.NotFoundException;
import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;

import org.springframework.stereotype.Service;

@Service
public class DatasetService {

    private final DatasetDao datasetDao;
    private final AssetUrlBuilder assetUrlBuilder;

    public DatasetService(DatasetDao datasetDao, AssetUrlBuilder assetUrlBuilder) {
        this.datasetDao = datasetDao;
        this.assetUrlBuilder = assetUrlBuilder;
    }

    public List<DatasetSummary> list() {
        return datasetDao.listAll().stream().map(this::withDisplaySpecUrl).toList();
    }

    public DatasetSummary requireById(String datasetId) {
        return datasetDao.findById(datasetId)
                .map(this::withDisplaySpecUrl)
                .orElseThrow(() -> new NotFoundException("Dataset not found: " + datasetId));
    }

    public DatasetDetail detail(String datasetId) {
        DatasetSummary dataset = requireById(datasetId);
        List<ModSummary> mods = datasetDao.listMods(datasetId);
        return new DatasetDetail(dataset, mods);
    }

    public PageResponse<ModSummary> listModsPage(
            String datasetId, String query, String sortField, boolean descending, PageRequest page) {
        DatasetSummary dataset = requireById(datasetId);
        return datasetDao.listModsPage(dataset.datasetId(), query, sortField, descending, page);
    }

    /** spec 文件外挂在 dataset 资源目录下：<assets>/<pack>/<ver>/<variant>/spec/<language>.json */
    private DatasetSummary withDisplaySpecUrl(DatasetSummary ds) {
        String url = assetUrlBuilder.build(ds, "spec/" + ds.language() + ".json", null);
        return new DatasetSummary(
                ds.datasetId(), ds.packSlug(), ds.packVersion(), ds.variant(),
                ds.language(), ds.displayName(), ds.schemaVersion(), ds.exporterVersion(),
                ds.minecraftVersion(), ds.createdAt(), ds.activePlugins(), url);
    }
}
