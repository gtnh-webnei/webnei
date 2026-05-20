package moe.takochan.webnei.fluid;

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
public class FluidController {

    private final FluidService fluidService;

    public FluidController(FluidService fluidService) {
        this.fluidService = fluidService;
    }

    @GetMapping("/fluids")
    public PageResponse<FluidSummary> list(
            @PathVariable String datasetId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String modId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return fluidService.list(datasetId, new FluidQuery(q, modId), PageRequest.of(page, size));
    }

    @GetMapping("/fluids/{fluidVariantId}")
    public FluidDetail detail(
            @PathVariable String datasetId,
            @PathVariable String fluidVariantId) {
        return fluidService.detail(datasetId, fluidVariantId);
    }

    @GetMapping("/fluid-mods")
    public List<String> mods(@PathVariable String datasetId) {
        return fluidService.listModIds(datasetId);
    }
}
