package moe.takochan.webnei.controller;

import java.util.List;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.model.dto.MobDetail;
import moe.takochan.webnei.model.dto.MobSummary;
import moe.takochan.webnei.model.dto.ModOptionDto;
import moe.takochan.webnei.model.dto.PageRequest;
import moe.takochan.webnei.service.DatasetResolver;
import moe.takochan.webnei.service.MobService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasets/{datasetId}")
public class MobController {

    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final int MAX_PAGE_SIZE = 192;

    private final DatasetResolver datasetResolver;
    private final MobService mobService;

    public MobController(DatasetResolver datasetResolver, MobService mobService) {
        this.datasetResolver = datasetResolver;
        this.mobService = mobService;
    }

    @GetMapping("/mobs")
    public PageResponse<MobSummary> listMobs(
            @PathVariable String datasetId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String modId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return mobService.listMobs(datasetResolver.resolve(datasetId), q, modId, PageRequest.of(page, size, DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE));
    }

    @GetMapping("/mobs/mods")
    public List<ModOptionDto> listMobMods(@PathVariable String datasetId) {
        return mobService.listMods(datasetResolver.resolve(datasetId));
    }

    @GetMapping("/mobs/{mobVariantId}")
    public MobDetail mobDetail(
            @PathVariable String datasetId,
            @PathVariable String mobVariantId) {
        return mobService.mobDetail(datasetResolver.resolve(datasetId), mobVariantId);
    }
}
