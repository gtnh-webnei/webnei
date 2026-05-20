package moe.takochan.webnei.dataset;

import java.util.List;

import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasets")
public class DatasetController {

    private final DatasetService datasetService;

    public DatasetController(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    @GetMapping
    public List<DatasetSummary> list() {
        return datasetService.list();
    }

    @GetMapping("/{datasetId}")
    public DatasetDetail detail(@PathVariable String datasetId) {
        return datasetService.detail(datasetId);
    }

    @GetMapping("/{datasetId}/mods/page")
    public PageResponse<ModSummary> listModsPage(
            @PathVariable String datasetId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "false") boolean desc,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return datasetService.listModsPage(
                datasetId, q, sort, desc, PageRequest.of(page, size == null ? 50 : size));
    }
}
