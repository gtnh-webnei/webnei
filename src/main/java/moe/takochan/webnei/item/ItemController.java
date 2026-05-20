package moe.takochan.webnei.item;

import java.util.List;

import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasets/{datasetId}")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/items")
    public PageResponse<NeiPanelEntryDto> listItems(
            @PathVariable String datasetId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String modId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return itemService.listPanel(datasetId, new ItemQuery(q, modId), PageRequest.of(page, size));
    }

    @GetMapping("/items/{itemVariantId}")
    public ItemDetailDto getItem(
            @PathVariable String datasetId,
            @PathVariable String itemVariantId) {
        return itemService.detail(datasetId, itemVariantId);
    }

    @GetMapping("/mods")
    public List<String> listMods(@PathVariable String datasetId) {
        return itemService.listModIds(datasetId);
    }
}
