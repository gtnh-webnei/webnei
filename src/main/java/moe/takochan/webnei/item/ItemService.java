package moe.takochan.webnei.item;

import java.util.List;

import moe.takochan.webnei.common.NotFoundException;
import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.dataset.DatasetService;
import moe.takochan.webnei.dataset.DatasetSummary;

import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private final DatasetService datasetService;
    private final ItemDao itemDao;

    public ItemService(DatasetService datasetService, ItemDao itemDao) {
        this.datasetService = datasetService;
        this.itemDao = itemDao;
    }

    public PageResponse<NeiPanelEntryDto> listPanel(String datasetId, ItemQuery query, PageRequest page) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        List<NeiPanelEntryDto> items = itemDao.listPanel(dataset, query, page);
        long total = itemDao.countPanel(dataset, query);
        return PageResponse.of(items, page, total);
    }

    public ItemDetailDto detail(String datasetId, String itemVariantId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return itemDao.findVariant(dataset, itemVariantId)
                .orElseThrow(() -> new NotFoundException("Item variant not found: " + itemVariantId));
    }

    public List<String> listModIds(String datasetId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return itemDao.listModIds(dataset);
    }
}
