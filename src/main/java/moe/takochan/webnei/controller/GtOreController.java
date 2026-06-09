package moe.takochan.webnei.controller;

import moe.takochan.webnei.model.dto.GtBartWorksOreDetail;
import moe.takochan.webnei.model.dto.GtBartWorksOreSummary;
import moe.takochan.webnei.model.dto.GtOreVeinDetail;
import moe.takochan.webnei.model.dto.GtOreVeinSummary;
import moe.takochan.webnei.model.dto.GtResourceListResponse;
import moe.takochan.webnei.model.dto.GtSmallOreDetail;
import moe.takochan.webnei.model.dto.GtSmallOreSummary;
import moe.takochan.webnei.model.dto.GtUndergroundFluidDetail;
import moe.takochan.webnei.model.dto.GtUndergroundFluidSummary;
import moe.takochan.webnei.service.DatasetResolver;
import moe.takochan.webnei.service.GtOreService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasets/{datasetId}/gt")
public class GtOreController {

    private final DatasetResolver datasetResolver;
    private final GtOreService gtOreService;

    public GtOreController(DatasetResolver datasetResolver, GtOreService gtOreService) {
        this.datasetResolver = datasetResolver;
        this.gtOreService = gtOreService;
    }

    @GetMapping("/ore-veins")
    public GtResourceListResponse<GtOreVeinSummary> listOreVeins(
            @PathVariable String datasetId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String dimension) {
        return gtOreService.listOreVeins(datasetResolver.resolve(datasetId), q, dimension);
    }

    @GetMapping("/ore-veins/{veinName}")
    public GtOreVeinDetail oreVeinDetail(@PathVariable String datasetId, @PathVariable String veinName) {
        return gtOreService.oreVeinDetail(datasetResolver.resolve(datasetId), veinName);
    }

    @GetMapping("/small-ores")
    public GtResourceListResponse<GtSmallOreSummary> listSmallOres(
            @PathVariable String datasetId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String dimension) {
        return gtOreService.listSmallOres(datasetResolver.resolve(datasetId), q, dimension);
    }

    @GetMapping("/small-ores/{oreGenName}")
    public GtSmallOreDetail smallOreDetail(@PathVariable String datasetId, @PathVariable String oreGenName) {
        return gtOreService.smallOreDetail(datasetResolver.resolve(datasetId), oreGenName);
    }

    @GetMapping("/underground-fluids")
    public GtResourceListResponse<GtUndergroundFluidSummary> listUndergroundFluids(
            @PathVariable String datasetId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String dimension) {
        return gtOreService.listUndergroundFluids(datasetResolver.resolve(datasetId), q, dimension);
    }

    @GetMapping("/underground-fluids/{fluidId}")
    public GtUndergroundFluidDetail undergroundFluidDetail(
            @PathVariable String datasetId, @PathVariable String fluidId) {
        return gtOreService.undergroundFluidDetail(datasetResolver.resolve(datasetId), fluidId);
    }

    @GetMapping("/bartworks-ores")
    public GtResourceListResponse<GtBartWorksOreSummary> listBartWorksOres(
            @PathVariable String datasetId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String dimension) {
        return gtOreService.listBartWorksOres(datasetResolver.resolve(datasetId), q, type, dimension);
    }

    @GetMapping("/bartworks-ores/{entryId}")
    public GtBartWorksOreDetail bartWorksOreDetail(@PathVariable String datasetId, @PathVariable String entryId) {
        return gtOreService.bartWorksOreDetail(datasetResolver.resolve(datasetId), entryId);
    }
}
