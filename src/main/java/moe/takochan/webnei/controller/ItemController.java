package moe.takochan.webnei.controller;

import moe.takochan.webnei.dto.common.PageResponse;
import moe.takochan.webnei.dto.item.ItemListEntry;
import moe.takochan.webnei.dto.item.ItemListRequest;
import moe.takochan.webnei.service.ItemCatalogService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/item")
public class ItemController {

    private final ItemCatalogService service;

    public ItemController(ItemCatalogService service) {
        this.service = service;
    }

    @PostMapping("/list")
    public PageResponse<ItemListEntry> list(@RequestBody ItemListRequest request) {
        return service.list(request.getDatasetId(), request.getQ(), request.getPage(), request.getSize());
    }
}
