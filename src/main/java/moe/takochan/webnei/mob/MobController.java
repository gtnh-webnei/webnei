package moe.takochan.webnei.mob;

import java.util.List;

import moe.takochan.webnei.common.ModOptionDto;
import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.mob.dto.MobDetail;
import moe.takochan.webnei.mob.dto.MobSummary;

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

    private final MobService mobService;

    public MobController(MobService mobService) {
        this.mobService = mobService;
    }

    @GetMapping("/mobs")
    public PageResponse<MobSummary> listMobs(
            @PathVariable String datasetId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String modId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return mobService.listMobs(datasetId, q, modId, PageRequest.of(page, size, DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE));
    }

    @GetMapping("/mobs/mods")
    public List<ModOptionDto> listMobMods(@PathVariable String datasetId) {
        return mobService.listMods(datasetId);
    }

    @GetMapping("/mobs/{mobVariantId}")
    public MobDetail mobDetail(
            @PathVariable String datasetId,
            @PathVariable String mobVariantId) {
        return mobService.mobDetail(datasetId, mobVariantId);
    }
}
