package moe.takochan.webnei.fluid;

import java.util.List;

import moe.takochan.webnei.common.NotFoundException;
import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.dataset.DatasetService;
import moe.takochan.webnei.dataset.DatasetSummary;

import org.springframework.stereotype.Service;

@Service
public class FluidService {

    private final DatasetService datasetService;
    private final FluidDao fluidDao;

    public FluidService(DatasetService datasetService, FluidDao fluidDao) {
        this.datasetService = datasetService;
        this.fluidDao = fluidDao;
    }

    public PageResponse<FluidSummary> list(String datasetId, FluidQuery query, PageRequest page) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        List<FluidSummary> items = fluidDao.list(dataset, query, page);
        long total = fluidDao.count(dataset, query);
        return PageResponse.of(items, page, total);
    }

    public FluidDetail detail(String datasetId, String fluidVariantId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return fluidDao.findVariant(dataset, fluidVariantId)
                .orElseThrow(() -> new NotFoundException("Fluid variant not found: " + fluidVariantId));
    }

    public List<String> listModIds(String datasetId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return fluidDao.listModIds(dataset);
    }
}
