package moe.takochan.webnei.catalog.api;

import moe.takochan.webnei.catalog.service.CatalogService;
import moe.takochan.webnei.common.api.PageResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasets/{datasetId}")
public class CatalogController {

    private final CatalogService service;

    public CatalogController(CatalogService service) {
        this.service = service;
    }

    @GetMapping("/items")
    public PageResponse<ItemListEntry> items(
            @PathVariable String datasetId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "60") int size) {
        return service.items(datasetId, q, page, size);
    }

    @GetMapping("/fluids")
    public PageResponse<FluidListEntry> fluids(
            @PathVariable String datasetId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "60") int size) {
        return service.fluids(datasetId, q, page, size);
    }
}
