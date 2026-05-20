package moe.takochan.webnei.dataset;

import java.util.List;

import moe.takochan.webnei.common.NotFoundException;
import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;

import org.springframework.stereotype.Service;

@Service
public class DatasetService {

    private final DatasetDao datasetDao;

    public DatasetService(DatasetDao datasetDao) {
        this.datasetDao = datasetDao;
    }

    public List<DatasetSummary> list() {
        return datasetDao.listAll();
    }

    public DatasetSummary requireById(String datasetId) {
        return datasetDao.findById(datasetId)
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
}
