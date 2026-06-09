package moe.takochan.webnei.controller;

import java.util.List;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.model.dto.ItemDetailDto;
import moe.takochan.webnei.model.dto.ModOptionDto;
import moe.takochan.webnei.model.dto.NeiPanelEntryDto;
import moe.takochan.webnei.model.dto.PageRequest;
import moe.takochan.webnei.model.query.ItemQuery;
import moe.takochan.webnei.service.DatasetResolver;
import moe.takochan.webnei.service.ItemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasets/{datasetId}")
public class ItemController {

    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final int MAX_PAGE_SIZE = 240;

    private final DatasetResolver datasetResolver;
    private final ItemService itemService;

    public ItemController(DatasetResolver datasetResolver, ItemService itemService) {
        this.datasetResolver = datasetResolver;
        this.itemService = itemService;
    }

    @GetMapping("/items")
    public PageResponse<NeiPanelEntryDto> listItems(
            @PathVariable String datasetId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String modId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return itemService.listPanel(datasetResolver.resolve(datasetId), new ItemQuery(q, modId), PageRequest.of(page, size, DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE));
    }

    @GetMapping("/items/{itemVariantId}")
    public ItemDetailDto getItem(
            @PathVariable String datasetId,
            @PathVariable String itemVariantId) {
        return itemService.detail(datasetResolver.resolve(datasetId), itemVariantId);
    }

    @GetMapping("/items/mods")
    public List<ModOptionDto> listMods(@PathVariable String datasetId) {
        return itemService.listMods(datasetResolver.resolve(datasetId));
    }
}
