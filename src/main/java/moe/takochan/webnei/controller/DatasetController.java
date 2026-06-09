package moe.takochan.webnei.controller;

import java.util.List;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.model.dto.DatasetSummary;
import moe.takochan.webnei.model.dto.ModSummary;
import moe.takochan.webnei.model.dto.PageRequest;
import moe.takochan.webnei.service.DatasetResolver;
import moe.takochan.webnei.service.DatasetService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasets")
public class DatasetController {

    private static final int DEFAULT_MOD_PAGE_SIZE = 50;
    private static final int MAX_MOD_PAGE_SIZE = 200;

    private final DatasetResolver datasetResolver;
    private final DatasetService datasetService;

    public DatasetController(DatasetResolver datasetResolver, DatasetService datasetService) {
        this.datasetResolver = datasetResolver;
        this.datasetService = datasetService;
    }

    @GetMapping
    public List<DatasetSummary> list() {
        return datasetService.list();
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
                datasetResolver.resolve(datasetId), q, sort, desc, PageRequest.of(page, size, DEFAULT_MOD_PAGE_SIZE, MAX_MOD_PAGE_SIZE));
    }
}
