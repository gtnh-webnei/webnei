package moe.takochan.webnei.gtore;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasets/{datasetId}/gt")
public class GtOreController {

    private final GtOreService gtOreService;

    public GtOreController(GtOreService gtOreService) {
        this.gtOreService = gtOreService;
    }

    @GetMapping("/ore-veins")
    public GtResourceListResponse<GtOreVeinSummary> listOreVeins(
            @PathVariable String datasetId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String dimension) {
        return gtOreService.listOreVeins(datasetId, q, dimension);
    }

    @GetMapping("/ore-veins/{veinName}")
    public GtOreVeinDetail oreVeinDetail(@PathVariable String datasetId, @PathVariable String veinName) {
        return gtOreService.oreVeinDetail(datasetId, veinName);
    }

    @GetMapping("/small-ores")
    public GtResourceListResponse<GtSmallOreSummary> listSmallOres(
            @PathVariable String datasetId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String dimension) {
        return gtOreService.listSmallOres(datasetId, q, dimension);
    }

    @GetMapping("/small-ores/{oreGenName}")
    public GtSmallOreDetail smallOreDetail(@PathVariable String datasetId, @PathVariable String oreGenName) {
        return gtOreService.smallOreDetail(datasetId, oreGenName);
    }

    @GetMapping("/underground-fluids")
    public GtResourceListResponse<GtUndergroundFluidSummary> listUndergroundFluids(
            @PathVariable String datasetId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String dimension) {
        return gtOreService.listUndergroundFluids(datasetId, q, dimension);
    }

    @GetMapping("/underground-fluids/{fluidId}")
    public GtUndergroundFluidDetail undergroundFluidDetail(
            @PathVariable String datasetId, @PathVariable String fluidId) {
        return gtOreService.undergroundFluidDetail(datasetId, fluidId);
    }

    @GetMapping("/bartworks-ores")
    public GtResourceListResponse<GtBartWorksOreSummary> listBartWorksOres(
            @PathVariable String datasetId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String dimension) {
        return gtOreService.listBartWorksOres(datasetId, q, type, dimension);
    }

    @GetMapping("/bartworks-ores/{entryId}")
    public GtBartWorksOreDetail bartWorksOreDetail(@PathVariable String datasetId, @PathVariable String entryId) {
        return gtOreService.bartWorksOreDetail(datasetId, entryId);
    }
}
