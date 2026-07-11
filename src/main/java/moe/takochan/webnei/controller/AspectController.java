package moe.takochan.webnei.controller;

import java.util.List;

import moe.takochan.webnei.dto.aspect.AspectDetail;
import moe.takochan.webnei.dto.aspect.AspectItemListEntry;
import moe.takochan.webnei.dto.aspect.AspectItemListRequest;
import moe.takochan.webnei.dto.aspect.AspectListEntry;
import moe.takochan.webnei.dto.aspect.AspectListRequest;
import moe.takochan.webnei.dto.common.PageResponse;
import moe.takochan.webnei.service.AspectCatalogService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aspect")
public class AspectController {

    private final AspectCatalogService service;

    public AspectController(AspectCatalogService service) {
        this.service = service;
    }

    @PostMapping("/list")
    public List<AspectListEntry> list(@RequestBody AspectListRequest request) {
        return service.list(request.getDatasetId());
    }

    @GetMapping("/{aspectId}")
    public AspectDetail detail(
            @PathVariable String aspectId,
            @RequestParam String datasetId) {
        return service.detail(datasetId, aspectId);
    }

    @PostMapping("/items/list")
    public PageResponse<AspectItemListEntry> listItems(@RequestBody AspectItemListRequest request) {
        return service.listItems(
                request.getDatasetId(),
                request.getAspectId(),
                request.getQ(),
                request.getPage(),
                request.getSize());
    }
}
