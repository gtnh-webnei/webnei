package moe.takochan.webnei.fluid;

import java.util.List;

import moe.takochan.webnei.common.ModOptionDto;
import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.dataset.DatasetResolver;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasets/{datasetId}")
public class FluidController {

    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final int MAX_PAGE_SIZE = 240;

    private final DatasetResolver datasetResolver;
    private final FluidService fluidService;

    public FluidController(DatasetResolver datasetResolver, FluidService fluidService) {
        this.datasetResolver = datasetResolver;
        this.fluidService = fluidService;
    }

    @GetMapping("/fluids")
    public PageResponse<FluidSummary> list(
            @PathVariable String datasetId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String modId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return fluidService.list(datasetResolver.resolve(datasetId), new FluidQuery(q, modId), PageRequest.of(page, size, DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE));
    }

    @GetMapping("/fluids/{fluidVariantId}")
    public FluidDetail detail(
            @PathVariable String datasetId,
            @PathVariable String fluidVariantId) {
        return fluidService.detail(datasetResolver.resolve(datasetId), fluidVariantId);
    }

    @GetMapping("/fluids/mods")
    public List<ModOptionDto> mods(@PathVariable String datasetId) {
        return fluidService.listMods(datasetResolver.resolve(datasetId));
    }
}
